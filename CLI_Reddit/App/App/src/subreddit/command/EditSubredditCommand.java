package subreddit.command;

import account.SessionService;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;
import io.StringReader;
import util.SubredditNames;

import java.util.Optional;

public class EditSubredditCommand implements SubredditCommand {
    private final SessionService sessionService;
    private final StringReader stringReader;

    public EditSubredditCommand(SessionService sessionService, StringReader stringReader) {
        this.sessionService = sessionService;
        this.stringReader = stringReader;
    }

    @Override
    public void execute() {
        if (sessionService.isLoggedIn()) {
            editSubreddit();
        }
    }

    public void editSubreddit() {
        String targetSub = SubredditNames.normalize(chooseSubreddit());
        Optional<Subreddit> found = SubredditRepository.findByName(targetSub);
        if (found.isEmpty() || found.get().getId() == null) {
            System.out.println("Subreddit not found");
            return;
        }

        String newTitle = stringReader.readString("Enter new title: ");
        String newDesc = stringReader.readString("Enter new description: ");
        try {
            SubredditRepository.updateSubreddit(found.get().getId(), newTitle, newDesc);
            System.out.println("Subreddit edited successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String chooseSubreddit() {
        System.out.println("Subreddits this user has made: ");
        SubredditRepository.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to edit: ");
    }
}
