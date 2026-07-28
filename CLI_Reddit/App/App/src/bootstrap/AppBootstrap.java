package bootstrap;

import account.AccountMenu;
import account.SessionService;
import io.*;
import menu.MenuDispatcher;
import post.PostView;
import post.command.CreatePostCommand;
import post.repository.PostMenu;
import post.repository.PostRepo;
import subreddit.SubredditMenu;

public final class AppBootstrap {

    public static AppContext wire() {
        ConsoleIO console = new ConsoleIO();
        StringReader stringReader = console;
        IntReader intReader = console;
        OutputWriter output = new FormattedOutputWriter(console);

        SessionService sessionService = new SessionService();
        PostRepo postRepo = new PostRepo(sessionService);

        AccountMenu accountMenu = AccountModule.create(stringReader, output, sessionService);
        PostView postView = PostingModule.createPostView(stringReader, output);
        CreatePostCommand createPostCommand = PostingModule.createCreatePostCommand(
                postView, sessionService
        );

        PostMenu postMenu = InteractionModule.create(
                stringReader, intReader, output, postView, postRepo
        );

        SubredditMenu subredditMenu = SubredditModule.create(sessionService, stringReader, output, postRepo, postView);

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
