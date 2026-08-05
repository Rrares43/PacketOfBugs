package subreddit.command;

import account.SessionService;
import com.google.gson.JsonObject;
import api.RedditApiClient;
import subreddit.Subreddit;
import api.SubredditClient;
import io.StringReader;

import java.util.Optional;

import static api.SubredditClient.loadSubreddits;

public class EditSubredditCommand implements SubredditCommand{
    private final SessionService sessionService;
    private final StringReader stringReader;

    public EditSubredditCommand(SessionService sessionService, StringReader stringReader) {
        this.sessionService = sessionService;
        this.stringReader = stringReader;
    }

    @Override
    public void execute(){
        if(sessionService.isLoggedIn()) {
            editSubreddit();
        }
    }

    public void editSubreddit(){
        String subredditName = chooseSubreddit();
        Optional<Subreddit> targetSub = SubredditClient.findByName(subredditName);
        try {
            if (targetSub.isPresent()) {
                Optional<JsonObject> subredditJson = RedditApiClient.getSubredditByName(targetSub.get().getName());
                if (subredditJson.isEmpty()) {
                    System.out.println("Subreddit not found.");
                    return;
                }
                String subId = subredditJson.get().get("id").getAsString();
                long creatorId = subredditJson.get().get("creatorId").getAsLong();
                long currentAccountId = sessionService.getCurrentAccountId();
                
                if (creatorId != currentAccountId) {
                    System.out.println("You do not have permission to edit this subreddit.");
                    return;
                }

                String newName = stringReader.readString("Enter new name:");
                String newDescription = stringReader.readString("Enter new description:");
                RedditApiClient.editSubreddit(targetSub.get().getName(), newName, newDescription, currentAccountId);
                System.out.println("Subreddit successfully edited.");
            }
        }
        catch (Exception e){
            System.out.println("Subreddit not found.");
        }
    }

    public String chooseSubreddit(){
        System.out.println("Subreddits this user has made: ");
        SubredditClient.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to edit: ");
    }
}
