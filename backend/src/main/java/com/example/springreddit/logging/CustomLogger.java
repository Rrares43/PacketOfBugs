package com.example.springreddit.logging;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public final class CustomLogger {
    private static final int MAX_ENTRIES = 50;
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final CustomLogger INSTANCE = new CustomLogger();

    private final List<String> logs = new ArrayList<>();

    private CustomLogger() {
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

    public synchronized List<String> getLogs() {
        return List.copyOf(logs);
    }

    private synchronized void add(String level, String message) {
        logs.add("[" + LocalDateTime.now().format(TIMESTAMP) + "] [" + level + "] " + message);
        if (logs.size() > MAX_ENTRIES) {
            logs.remove(0);
        }
    }

    private String format(String message, Object... arguments) {
        String formatted = message;
        for (Object argument : arguments) {
            formatted = formatted.replaceFirst("\\{\\}", java.util.regex.Matcher.quoteReplacement(String.valueOf(argument)));
        }
        return formatted;
    }
}
