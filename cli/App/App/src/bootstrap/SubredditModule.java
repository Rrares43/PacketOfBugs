package bootstrap;

import account.SessionService;
import post.PostView;
import api.PostClient;
import subreddit.SubredditMenu;
import subreddit.command.CreateSubredditCommand;
import subreddit.command.DeleteSubredditCommand;
import subreddit.command.EditSubredditCommand;
import io.OutputWriter;
import io.StringReader;

final class SubredditModule {
    private SubredditModule() {
    }

    static SubredditMenu create(SessionService sessionService, StringReader stringReader, OutputWriter output, PostClient postClient, PostView postView) {
        SubredditMenu subredditMenu = new SubredditMenu(sessionService, stringReader, output);
        subredditMenu.registerCommand("1", new CreateSubredditCommand(sessionService, stringReader, output));
        subredditMenu.registerCommand("2", new EditSubredditCommand(sessionService, stringReader));
        subredditMenu.registerCommand("3", new DeleteSubredditCommand(stringReader, sessionService));
        return subredditMenu;
    }
}
