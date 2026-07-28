package logger.command;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import menu.LoggerSubCommand;
import persistence.RedditApiClient;

/**
 * Fetches application logs from the Spring Boot backend ({@code GET /api/logs}).
 */
public class ShowLogOptions implements LoggerSubCommand {

    @Override
    public String getNotificationText() {
        return "Show all logs (from server)";
    }

    @Override
    public boolean execute() {
        try {
            JsonArray logs = RedditApiClient.getLogs();
            if (logs.isEmpty()) {
                System.out.println("No logs available on the server.");
                return true;
            }

            System.out.println("--- SERVER LOGS ---");
            for (JsonElement element : logs) {
                System.out.println(formatLogEntry(element));
            }
        } catch (Exception e) {
            System.out.println("Failed to fetch logs: " + e.getMessage());
        }
        return true;
    }

    private String formatLogEntry(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "";
        }
        if (element.isJsonPrimitive()) {
            return element.getAsString();
        }
        if (!element.isJsonObject()) {
            return element.toString();
        }

        JsonObject obj = element.getAsJsonObject();
        StringBuilder sb = new StringBuilder();
        if (obj.has("timestamp") && !obj.get("timestamp").isJsonNull()) {
            sb.append("[").append(obj.get("timestamp").getAsString()).append("] ");
        } else if (obj.has("createdAt") && !obj.get("createdAt").isJsonNull()) {
            sb.append("[").append(obj.get("createdAt").getAsString()).append("] ");
        }
        if (obj.has("level") && !obj.get("level").isJsonNull()) {
            sb.append("[").append(obj.get("level").getAsString()).append("] ");
        }
        if (obj.has("message") && !obj.get("message").isJsonNull()) {
            sb.append(obj.get("message").getAsString());
        } else {
            sb.append(obj);
        }
        return sb.toString();
    }
}
