package logger;

import io.TextFormatter;
import menu.LoggerSubCommand;
import api.RedditApiClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ShowLogOptions implements LoggerSubCommand {

    @Override
    public String getNotificationText() {
        return "Show all logs";
    }

    @Override
    public boolean execute() {
        final JsonArray allLogs;
        try {
            allLogs = RedditApiClient.getLogs();
        } catch (Exception e) {
            System.out.println(TextFormatter.error("Unable to retrieve backend logs: " + e.getMessage()));
            return true;
        }

        if (allLogs.isEmpty()) {
            System.out.println(TextFormatter.warning("No logs available."));
            return true;
        }

        System.out.println(TextFormatter.header(" DISPLAYING CAPTURED LOGS"));
        for (JsonElement log : allLogs) {
            String logLine = log.getAsString();
            if (logLine.contains("[ERROR]")) {
                System.out.println(TextFormatter.error(logLine));
            } else if (logLine.contains("[WARN]")) {
                System.out.println(TextFormatter.warning(logLine));
            } else {
                System.out.println(logLine);
            }
        }
        return true;
    }
}
