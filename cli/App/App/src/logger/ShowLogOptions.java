package logger;

import io.OutputWriter;
import io.TextFormatter;
import api.RedditApiClient;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ShowLogOptions implements LoggerSubCommand {
    private final OutputWriter output;

    public ShowLogOptions(OutputWriter output) {
        this.output = output;
    }
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
            output.write(TextFormatter.error("Unable to retrieve backend logs: " + e.getMessage()));
            return true;
        }

        if (allLogs.isEmpty()) {
            output.write(TextFormatter.warning("No logs available."));
            return true;
        }

        output.write(TextFormatter.header(" DISPLAYING CAPTURED LOGS"));
        for (JsonElement log : allLogs) {
            String logLine = log.getAsString();
            if (logLine.contains("[ERROR]")) {
                output.write(TextFormatter.error(logLine));
            } else if (logLine.contains("[WARN]")) {
                output.write(TextFormatter.warning(logLine));
            } else {
                output.write(logLine);
            }
        }
        return true;
    }
}
