package bootstrap;

import account.AccountMenu;
import account.SessionService;
import io.*;
import subreddit.SubredditMenu;
import post.repository.PostMenu;
import post.repository.PostRepo;
import logger.LogLevel;
import logger.Logger;
import menu.MenuDispatcher;
import post.PostView;
import post.command.CreatePostCommand;

public final class AppBootstrap {

    public static AppContext wire() {
        ConsoleIO console = new ConsoleIO();
        StringReader stringReader = console;
        IntReader intReader = console;
        OutputWriter output = new FormattedOutputWriter(console);

        Logger logger = Logger.getInstance();
        logger.log(LogLevel.INFO, "Application Started");

        SessionService sessionService = new SessionService();
        PostRepo postRepo = new PostRepo(sessionService);

        AccountMenu accountMenu = AccountModule.create(stringReader, output, sessionService);
        PostView postView = PostingModule.createPostView(stringReader, output);
        CreatePostCommand createPostCommand = PostingModule.createCreatePostCommand(
                postView, postRepo, sessionService
        );

        PostMenu postMenu = InteractionModule.create(
                stringReader, intReader, output, postView, postRepo, logger
        );

        SubredditMenu subredditMenu = SubredditModule.create(sessionService, stringReader, output, postRepo, postView);

        MenuDispatcher dispatcher = MenuModule.create(
                output,
                stringReader,
                logger,
                accountMenu,
                createPostCommand,
                postMenu,
                subredditMenu
        );

        return new AppContext(sessionService, stringReader, output, dispatcher);
    }
}
