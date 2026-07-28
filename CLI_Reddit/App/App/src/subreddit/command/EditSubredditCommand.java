package subreddit.command;

import account.SessionService;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;
import io.StringReader;

import java.util.List;

import static subreddit.repository.SubredditRepository.loadSubreddits;

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
        List<Subreddit> subreddits = loadSubreddits();
        String targetSub = chooseSubreddit();
        boolean found = false;
        for(Subreddit sub : subreddits){
            if(sub.getName().equals(targetSub)){
                String newTitle = stringReader.readString("Enter new title: ");
                if(newTitle.startsWith("r/")){
                    sub.setName(newTitle);
                }
                else{
                    sub.setName("r/" + newTitle);
                }
                String newDesc = stringReader.readString("Enter new description: ");
                sub.setDescription(newDesc);
                found = true;
                break;
            }
        }
        if(found){
            SubredditRepository.writeSubreddits(subreddits);
            System.out.println("Subreddit edited successfully!");
        }
        else{
            System.out.println("Subreddit not found");
        }
    }

    public String chooseSubreddit(){
        System.out.println("Subreddits this user has made: ");
        SubredditRepository.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to edit: ");
    }
}
