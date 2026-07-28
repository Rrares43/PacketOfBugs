package subreddit.command;

import account.SessionService;
import io.StringReader;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;

import java.util.List;

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
        List<Subreddit> subreddits = SubredditRepository.loadSubreddits();
        String targetSub = chooseSubreddit();
        boolean found = false;
        for(Subreddit sub : subreddits){
            if(sub.getName().equals(targetSub)){
                subreddits.remove(sub);
                SubredditRepository.writeSubreddits(subreddits);
                found = true;
                break;
            }
        }

        if(found){
            SubredditRepository.writeSubreddits(subreddits);
            System.out.println("Subreddit deleted successfully!");
        }
        else{
            System.out.println("Subreddit not found");
        }
    }

    public String chooseSubreddit(){
        System.out.println("Subreddits this user has made: ");
        SubredditRepository.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to delete: ");
    }
}
