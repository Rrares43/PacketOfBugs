package bootstrap;

import account.AccountMenu;
import subreddit.SubredditMenu;
import post.repository.PostMenu;
import logger.Logger;
import menu.AccountCommand;
import menu.InterractionCommand;
import menu.LoggerCommand;
import menu.MenuDispatcher;
import menu.PostCommand;
import menu.SubredditCommand;
import io.OutputWriter;
import io.StringReader;
import post.command.CreatePostCommand;

final class MenuModule {
    private MenuModule() {
    }

    static MenuDispatcher create(OutputWriter output,
                                 StringReader stringReader,
                                 Logger logger,
                                 AccountMenu accountMenu,
                                 CreatePostCommand createPostCommand,
                                 PostMenu postMenu,
                                 SubredditMenu subredditMenu) {
        MenuDispatcher dispatcher = new MenuDispatcher(output);
        dispatcher.registerCommand("1", new AccountCommand(accountMenu));
        dispatcher.registerCommand("2", new PostCommand(createPostCommand));
        dispatcher.registerCommand("3", new InterractionCommand(postMenu));
        dispatcher.registerCommand("4", new SubredditCommand(subredditMenu));
        dispatcher.registerCommand("5", new LoggerCommand(logger, stringReader, output));
        return dispatcher;
    }
}
