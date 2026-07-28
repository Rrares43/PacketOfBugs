package subreddit.command;

import account.SessionService;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;
import subreddit.model.SubDescription;
import subreddit.model.SubName;

import java.util.Objects;

public class CreateSubredditCommand implements SubredditCommand {
    private final SessionService sessionService;

    public CreateSubredditCommand(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void execute(){
        System.out.println("Enter data or 0 to return to the previous menu:");
        String subredditName = SubName.ask();
        if(Objects.equals(subredditName, "0")){
            return;
        }
        String subredditDescription = SubDescription.ask();
        if(Objects.equals(subredditDescription, "0")){
            return;
        }
        Subreddit subreddit = new Subreddit(subredditName, subredditDescription, sessionService.getCurrentUsername());
        SubredditRepository.saveSubreddit(subreddit);
    }
}
