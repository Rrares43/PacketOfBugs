package bootstrap;

import account.SessionService;
import io.TextFormatter;
import menu.MenuDispatcher;
import io.OutputWriter;
import io.StringReader;

public class ApplicationLoop {
    private final SessionService sessionService;
    private final StringReader stringReader;
    private final OutputWriter output;
    private final MenuDispatcher dispatcher;

    public ApplicationLoop(AppContext context) {
        this.sessionService = context.getSessionService();
        this.stringReader = context.getStringReader();
        this.output = context.getOutput();
        this.dispatcher = context.getDispatcher();
    }

    public void run() {
        while (true) {
            if (!sessionService.isLoggedIn()) {
                dispatcher.execute("1");
                continue;
            }

            output.write(TextFormatter.header("\n--- MAIN MENU ---"));
            output.write("0. Exit");
            output.write("1. Account menu");
            output.write("2. Post Options");
            output.write("3. Interaction");
            output.write("4. Subreddit menu");
            output.write("5. Logger");
            output.write("");
            output.write(TextFormatter.separator(50));

            String choice = stringReader.readString("Select your choice (0/1/2/3/4/5): ");
            if ("0".equals(choice)) {
                output.write("Application is closing");
                break;
            }
            dispatcher.execute(choice);
        }
    }
}
