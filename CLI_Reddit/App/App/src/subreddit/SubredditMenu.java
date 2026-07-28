package subreddit;

import account.SessionService;
import io.TextFormatter;
import subreddit.command.SubredditCommand;
import io.OutputWriter;
import io.StringReader;

import java.util.HashMap;
import java.util.Map;

public class SubredditMenu {
    Map<String, SubredditCommand> commands;
    private final SessionService sessionService;
    private final StringReader stringReader;
    private final OutputWriter output;
    private boolean running;

    public SubredditMenu(SessionService sessionService, StringReader stringReader, OutputWriter output) {
        this.sessionService = sessionService;
        this.stringReader = stringReader;
        this.output = output;
        this.commands = new HashMap<>();

        commands.put("0", () -> this.running = false);
    }

    public void registerCommand(String key, SubredditCommand command) {
        commands.put(key, command);
    }

    public void startSubredditMenu() {
        running = true;
        while (running){
            String choice;
            output.write(TextFormatter.header("\n--- SUBREDDIT MENU ---"));
            output.write("0. Exit");
            output.write("1. Create Subreddit");
            output.write("2. View Subreddit");
            output.write("3. Edit Subreddit");
            output.write("4. Delete Subreddit");
            output.write(TextFormatter.separator(50));
            output.write("");

            choice = stringReader.readString("Select an option (0/1/2/3/4): ");
            if(choice.equals("0")){
                running = false;
            }
            else if(!(choice.equals("1") ||choice.equals("2") || choice.equals("3") || choice.equals("4"))){
                output.write("Invalid option! Please try again.");
            }
            else{
                SubredditCommand command = commands.get(choice);
                if (command != null){
                    try {
                        command.execute();
                    }
                    catch (Exception e){
                        output.write("Error: " + e.getMessage());
                    }
                }
            }
        }
    }

}
