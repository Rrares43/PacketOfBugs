package logger;

public interface LoggerSubCommand {
    String getNotificationText();
    boolean execute();
}