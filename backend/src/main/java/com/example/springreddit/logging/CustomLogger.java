package com.example.springreddit.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public final class CustomLogger {
    private static final int MAX_ENTRIES = 50;
    private static final int QUEUE_CAPACITY = 1024;
    private static final Path LOG_FILE = Path.of("logs", "application.log");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CustomLogger INSTANCE = new CustomLogger();
    private static final Logger SLF4J_LOGGER = LoggerFactory.getLogger("ApplicationLog");
    private static final LogEntry POISON_PILL = new LogEntry(null, null, null);

    private final List<String> logs = new ArrayList<>();
    private final BlockingQueue<LogEntry> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final Thread workerThread;
    private final AtomicBoolean shutdownRequested = new AtomicBoolean(false);

    private CustomLogger() {
        workerThread = new Thread(this::consumeLogEntries, "custom-logger-worker");
        workerThread.setDaemon(false);
        workerThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdownGracefully, "custom-logger-shutdown"));
    }

    public static CustomLogger getInstance() {
        return INSTANCE;
    }

    public void info(String message) {
        add("INFO", message);
    }

    public void info(String message, Object... arguments) {
        add("INFO", format(message, arguments));
    }

    public void warn(String message) {
        add("WARN", message);
    }

    public void warn(String message, Object... arguments) {
        add("WARN", format(message, arguments));
    }

    public void error(String message, Object... arguments) {
        add("ERROR", format(message, arguments));
    }

    public synchronized List<String> getLogs() {
        return List.copyOf(logs);
    }

    void shutdownGracefully() {
        if (!shutdownRequested.compareAndSet(false, true)) {
            return;
        }

        try {
            queue.put(POISON_PILL);
            workerThread.join(10_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void add(String level, String message) {
        if (message == null) {
            return;
        }

        String lowerMessage = message.toLowerCase();
        if (lowerMessage.contains("select ") ||
                lowerMessage.contains("insert into") ||
                lowerMessage.contains("update ") ||
                lowerMessage.contains("delete from") ||
                lowerMessage.contains("hibernate") ||
                lowerMessage.contains("jdbc") ||
                lowerMessage.contains("binding parameter")) {
            return;
        }

        String formattedEntry = "[" + LocalDateTime.now().format(TIMESTAMP) + "] [" + level + "] " + message;
        try {
            queue.put(new LogEntry(formattedEntry, level, message));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void consumeLogEntries() {
        try {
            Files.createDirectories(LOG_FILE.getParent());
        } catch (IOException e) {
            SLF4J_LOGGER.error("Failed to create log directory for {}", LOG_FILE, e);
            return;
        }

        try (BufferedWriter writer = Files.newBufferedWriter(
                LOG_FILE,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND)) {
            while (true) {
                LogEntry entry;
                try {
                    entry = queue.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }

                if (entry == POISON_PILL) {
                    writer.flush();
                    break;
                }

                appendToMemory(entry.formattedEntry());
                writeToFile(writer, entry.formattedEntry());
                forwardToSlf4j(entry.level(), entry.message());
            }
        } catch (IOException e) {
            SLF4J_LOGGER.error("Failed to write application log to {}", LOG_FILE, e);
        }
    }

    private synchronized void appendToMemory(String formattedEntry) {
        logs.add(formattedEntry);
        if (logs.size() > MAX_ENTRIES) {
            logs.remove(0);
        }
    }

    private void writeToFile(BufferedWriter writer, String formattedEntry) throws IOException {
        writer.write(formattedEntry);
        writer.newLine();
        writer.flush();
    }

    private void forwardToSlf4j(String level, String message) {
        switch (level) {
            case "WARN" -> SLF4J_LOGGER.warn(message);
            case "ERROR" -> SLF4J_LOGGER.error(message);
            default -> SLF4J_LOGGER.info(message);
        }
    }

    private String format(String message, Object... arguments) {
        String formatted = message;
        for (Object argument : arguments) {
            formatted = formatted.replaceFirst("\\{\\}", java.util.regex.Matcher.quoteReplacement(String.valueOf(argument)));
        }
        return formatted;
    }

    private record LogEntry(String formattedEntry, String level, String message) {
    }
}
