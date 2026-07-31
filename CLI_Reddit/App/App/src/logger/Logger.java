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
 * Application logger: in-memory events plus append/read of the existing
 * {@code CLI_Reddit/App/data/app_log.txt} next to the JSON data files.
 */
public class Logger {
    private static Logger instance;
    private static final DateTimeFormatter TIMESTAMP =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> memoryLogs = new ArrayList<>();
    private final Path logFile;

    private Logger() {
        this.logFile = Path.of("data", "app_log.txt");
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
        appendToFile(formattedMessage);
    }

    public void printLogsToConsole() {
        List<String> allLogs = readFileLogs();
        if (allLogs.isEmpty()) {
            allLogs = new ArrayList<>(memoryLogs);
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

    private void appendToFile(String formattedMessage) {
        if (logFile == null) {
            return;
        }
        try {
            Files.writeString(
                    logFile,
                    formattedMessage + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException ignored) {
            // File logging is optional; in-memory logs remain available.
        }
    }

    private List<String> readFileLogs() {
        List<String> lines = new ArrayList<>();
        if (logFile == null || !Files.exists(logFile)) {
            return lines;
        }
        try {
            lines.addAll(Files.readAllLines(logFile));
        } catch (IOException e) {
            System.out.println(TextFormatter.error("Error: Could not read the log file."));
        }
        return lines;
    }
}
