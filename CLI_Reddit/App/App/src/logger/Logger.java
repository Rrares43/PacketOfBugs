package logger;

import io.TextFormatter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Logger {
    private static Logger instance;
    private static final String FILE_NAME = "App/data/app_log.txt";

    private Logger() {}

    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }

    public void log(LogLevel level, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedMessage = "[" + timestamp + "] [" + level + "] " + message;

        try (FileWriter fw = new FileWriter(FILE_NAME, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(formattedMessage);
        } catch (IOException e) {
            System.out.println("Error: Could not write to file");
            System.out.println(formattedMessage);
        }
    }

    public void printLogsToConsole() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println(TextFormatter.warning("No logs available (log file does not exist)."));
            return;
        }

        List<String> allLogs = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                allLogs.add(fileScanner.nextLine());
            }
        } catch (IOException e) {
            System.out.println(TextFormatter.error("Error: Could not read the log file."));
            return;
        }

        if (allLogs.isEmpty()) {
            System.out.println(TextFormatter.warning("No logs available."));
            return;
        }
        int startIndex = 0;
        if (allLogs.size() > 10) {
            startIndex = allLogs.size() - 10;
        }

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
}