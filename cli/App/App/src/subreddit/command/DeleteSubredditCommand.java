package subreddit.command;

import account.SessionService;
import com.google.gson.JsonObject;
import io.StringReader;
import api.RedditApiClient;
import subreddit.Subreddit;
import api.SubredditClient;

import java.util.Optional;

public class DeleteSubredditCommand implements SubredditCommand{
    private final StringReader stringReader;
    private final SessionService sessionService;

    public DeleteSubredditCommand(StringReader stringReader, SessionService sessionService) {
        this.stringReader = stringReader;
        this.sessionService = sessionService;
    }

    @Override
    public void execute(){
        if(sessionService.isLoggedIn()) {
            deleteSubreddit();
        }
    }

    private void deleteSubreddit(){
        String targetSubreddit = chooseSubreddit();
        Optional<Subreddit> targetSub = SubredditClient.findByName(targetSubreddit);

        if(targetSub.isPresent()){
            try {
                Optional<JsonObject> subredditJson = RedditApiClient.getSubredditByName(targetSub.get().getName());
                if (subredditJson.isEmpty()) {
                    System.out.println("Subreddit not found.");
                    return;
                }
                long subId = subredditJson.get().get("id").getAsLong();
                long creatorId = subredditJson.get().get("creatorId").getAsLong();
                Long currentAccountId = sessionService.getCurrentAccountId();
                if (currentAccountId == null) {
                    currentAccountId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
                }

                if (creatorId == currentAccountId) {
                    RedditApiClient.deleteSubreddit(subId, currentAccountId);
                    System.out.println("Subreddit successfully deleted.");
                } else {
                    System.out.println("You do not have permission to delete this subreddit.");
                }
            }
            catch (Exception e){
                System.out.println("Subreddit not found.");
            }
        }

    }

    public String chooseSubreddit(){
        System.out.println("Subreddits this user has made: ");
        SubredditClient.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to delete: ");
    }
}
