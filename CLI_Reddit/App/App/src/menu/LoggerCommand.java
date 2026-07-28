package menu;

import io.TextFormatter;
import logger.Logger;
import logger.command.BacktoMain;
import logger.command.ShowLogOptions;
import io.OutputWriter;
import io.StringReader;

import java.util.ArrayList;
import java.util.List;

public class LoggerCommand implements MenuCommand {

    private final StringReader stringReader;
    private final OutputWriter output;
    private final List<LoggerSubCommand> options = new ArrayList<>();

    public LoggerCommand(Logger logger, StringReader stringReader, OutputWriter output) {
        this.stringReader = stringReader;
        this.output = output;

        this.options.add(new ShowLogOptions(logger));

        this.options.add(new BacktoMain(output));
    }

    @Override
    public void execute() {
        boolean stayInMenu = true;

        while (stayInMenu) {
            output.write(TextFormatter.header("\n--- LOGGER SETTINGS ---"));

            for (int i = 0; i < options.size(); i++) {
                output.write((i + 1) + ". " + options.get(i).getNotificationText());
            }
            output.write(TextFormatter.separator(23));
            output.write("");

            String choice = stringReader.readString("Select option: ");

            try {
                int numarSelectat = Integer.parseInt(choice);
                int indexInLista = numarSelectat - 1;

                if (indexInLista >= 0 && indexInLista < options.size()) {
                    stayInMenu = options.get(indexInLista).execute();
                } else {
                    output.write("Invalid option.");
                }
            } catch (NumberFormatException e) {
                output.write("Please enter a valid number.");
            }
        }
    }
}