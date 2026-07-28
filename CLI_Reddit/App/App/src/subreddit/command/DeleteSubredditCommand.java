package subreddit.command;

import account.SessionService;
import io.StringReader;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;
import util.SubredditNames;

import java.util.Optional;

public class DeleteSubredditCommand implements SubredditCommand {
    private final StringReader stringReader;
    private final SessionService sessionService;

    public DeleteSubredditCommand(StringReader stringReader, SessionService sessionService) {
        this.stringReader = stringReader;
        this.sessionService = sessionService;
    }

    @Override
    public void execute() {
        if (sessionService.isLoggedIn()) {
            deleteSubreddit();
        }
    }

    private void deleteSubreddit() {
        String targetSub = SubredditNames.normalize(chooseSubreddit());
        Optional<Subreddit> found = SubredditRepository.findByName(targetSub);
        if (found.isEmpty() || found.get().getId() == null) {
            System.out.println("Subreddit not found");
            return;
        }

        try {
            SubredditRepository.deleteSubreddit(found.get().getId());
            System.out.println("Subreddit deleted successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public String chooseSubreddit() {
        System.out.println("Subreddits this user has made: ");
        SubredditRepository.listSubsMadebyUser(sessionService.getCurrentUsername());
        return stringReader.readString("Choose subreddit to delete: ");
    }
}
