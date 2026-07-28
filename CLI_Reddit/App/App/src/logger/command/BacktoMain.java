package logger.command;

import menu.LoggerSubCommand;
import io.OutputWriter;

public class BacktoMain implements LoggerSubCommand {
    private final OutputWriter output;

    public BacktoMain(OutputWriter output) {
        this.output = output;
    }
    @Override
    public String getNotificationText() {
        return "Back to Main Menu";
    }

    @Override
    public boolean execute() {
        output.write("Going back to Main Menu");
        return false;
    }
}