package subreddit.command;

import account.SessionService;
import io.StringReader;
import persistence.RedditApiClient;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;

import java.util.List;
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
        Optional<Subreddit> targetSub = SubredditRepository.findByName(targetSubreddit);

        if(targetSub.isPresent()){
            try {
                long subId = RedditApiClient.getSubredditByName(targetSub.get().getName()).get().getAsJsonObject().get("id").getAsLong();
                long creatorId = sessionService.getCurrentAccountId();

                if (creatorId == RedditApiClient.getSubredditByName(targetSub.get().getName()).get().getAsJsonObject().get("creatorId").getAsLong()) {
                    RedditApiClient.deleteSubreddit(subId);
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
        SubredditRepository.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to delete: ");
    }
}
