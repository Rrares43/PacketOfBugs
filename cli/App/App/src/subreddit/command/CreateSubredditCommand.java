package subreddit.command;

import account.SessionService;
import io.OutputWriter;
import io.StringReader;
import subreddit.Subreddit;
import api.SubredditClient;

import java.util.Objects;

public class CreateSubredditCommand implements SubredditCommand {
    private final SessionService sessionService;
    private final StringReader stringReader;
    private final OutputWriter output;

    public CreateSubredditCommand(SessionService sessionService, StringReader stringReader, OutputWriter output) {
        this.sessionService = sessionService;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(){
        System.out.println("Enter data or 0 to return to the previous menu:");
        String subredditName = stringReader.readString("Subreddit name: ");
        if(Objects.equals(subredditName, "0")){
            return;
        }
        if(subredditName.isBlank()){
            output.write("Subreddit name cannot be empty!");
            return;
        }
        String subredditDescription = stringReader.readString("Subreddit description: ");
        if(Objects.equals(subredditDescription, "0")){
            return;
        }
        if(subredditDescription.isBlank()){
            output.write("Subreddit description cannot be empty!");
            return;
        }
        Subreddit subreddit = new Subreddit(subredditName, subredditDescription, sessionService.getCurrentUsername());
        SubredditClient.saveSubreddit(subreddit);
    }
}
