package logger.command;

import logger.Logger;
import menu.LoggerSubCommand;

public class ShowLogOptions implements LoggerSubCommand {
    private final Logger logger;

    public ShowLogOptions(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getNotificationText() {
        return "Show all logs";
    }

    @Override
    public boolean execute() {
        logger.printLogsToConsole();
        return true;
    }
}