package bootstrap;

import account.AccountMenu;
import account.SessionService;
import io.ConsoleIO;
import io.FormattedOutputWriter;
import io.IntReader;
import io.OutputWriter;
import io.StringReader;
import menu.MenuDispatcher;
import post.PostView;
import post.command.CreatePostCommand;
import post.PostMenu;
import api.PostClient;
import subreddit.SubredditMenu;

public final class AppBootstrap {

    public static AppContext wire() {
        ConsoleIO console = new ConsoleIO();
        StringReader stringReader = console;
        IntReader intReader = console;
        OutputWriter output = new FormattedOutputWriter(console);

        SessionService sessionService = new SessionService();
        PostClient postClient = new PostClient(sessionService);

        AccountMenu accountMenu = AccountModule.create(stringReader, output, sessionService);
        PostView postView = PostingModule.createPostView(stringReader, output);
        CreatePostCommand createPostCommand = PostingModule.createCreatePostCommand(
                postView, postClient, sessionService
        );

        PostMenu postMenu = InteractionModule.create(
                stringReader, intReader, output, postView, sessionService, postClient
        );

        SubredditMenu subredditMenu = SubredditModule.create(sessionService, stringReader, output, postClient, postView);

        MenuDispatcher dispatcher = MenuModule.create(
                output,
                stringReader,
                accountMenu,
                createPostCommand,
                postMenu,
                subredditMenu
        );

        return new AppContext(sessionService, stringReader, output, dispatcher);
    }
}
