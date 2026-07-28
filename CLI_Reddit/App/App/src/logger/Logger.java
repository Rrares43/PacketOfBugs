package logger;

import io.TextFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * In-memory logger with optional file persistence.
 * File writing is best-effort: if the legacy path is unavailable, logs still
 * remain available in memory for console display without noisy I/O errors.
 */
public class Logger {
    private static Logger instance;
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> memoryLogs = new ArrayList<>();
    private final Path logFile;

    private Logger() {
        this.logFile = resolveLogFile();
    }

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(TIMESTAMP);
        String formattedMessage = "[" + timestamp + "] [" + level + "] " + message;
        memoryLogs.add(formattedMessage);

        if (logFile == null) {
            return;
        }

        try {
            Files.createDirectories(logFile.getParent());
            Files.writeString(
                    logFile,
                    formattedMessage + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // File logging is optional; in-memory logs remain available.
        }
    }

    public void printLogsToConsole() {
        if (memoryLogs.isEmpty() && (logFile == null || !Files.exists(logFile))) {
            System.out.println(TextFormatter.warning("No logs available."));
            return;
        }

        List<String> allLogs = new ArrayList<>(memoryLogs);
        if (allLogs.isEmpty() && logFile != null && Files.exists(logFile)) {
            try {
                allLogs.addAll(Files.readAllLines(logFile));
            } catch (IOException e) {
                System.out.println(TextFormatter.error("Error: Could not read the log file."));
                return;
            }
        }

        if (allLogs.isEmpty()) {
            System.out.println(TextFormatter.warning("No logs available."));
            return;
        }

        int startIndex = Math.max(0, allLogs.size() - 10);
        System.out.println(TextFormatter.header(" DISPLAYING LAST 10 LOGS"));
        for (int i = startIndex; i < allLogs.size(); i++) {
            String logLine = allLogs.get(i);
            if (logLine.contains("[ERROR]")) {
                System.out.println(TextFormatter.error(logLine));
            } else if (logLine.contains("[WARNING]")) {
                System.out.println(TextFormatter.warning(logLine));
            } else {
                System.out.println(logLine);
            }
        }
    }

    private static Path resolveLogFile() {
        Path[] candidates = {
                Path.of("App", "data", "app_log.txt"),
                Path.of("data", "app_log.txt"),
                Path.of("CLI_Reddit", "App", "data", "app_log.txt"),
                Path.of(System.getProperty("user.dir"), "App", "data", "app_log.txt"),
                Path.of(System.getProperty("java.io.tmpdir"), "buggit", "app_log.txt")
        };

        for (Path candidate : candidates) {
            try {
                Path parent = candidate.getParent();
                if (parent != null && (Files.isDirectory(parent) || parent.toFile().mkdirs())) {
                    return candidate.toAbsolutePath().normalize();
                }
            } catch (Exception ignored) {
                // try next candidate
            }
        }
        return null;
    }
}
