package menu;

import io.OutputWriter;

import java.util.HashMap;
import java.util.Map;

public class MenuDispatcher {
    private final Map<String, MenuCommand> commands = new HashMap<>();
    private final OutputWriter output;

    public MenuDispatcher(OutputWriter output) {
        this.output = output;
    }

    public void registerCommand(String choice, MenuCommand command) {
        commands.put(choice, command);
    }

    public void execute(String choice) {
        MenuCommand command = commands.get(choice);
        if (command != null) {
            command.execute();
        } else {
            output.write("Invalid Input");
        }
    }
}
