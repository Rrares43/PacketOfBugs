package bootstrap;

import account.AccountMenu;
import menu.*;
import subreddit.SubredditMenu;
import post.PostMenu;
import io.OutputWriter;
import io.StringReader;
import post.command.CreatePostCommand;

final class MenuModule {
    private MenuModule() {
    }

    static MenuDispatcher create(OutputWriter output,
                                 StringReader stringReader,
                                 AccountMenu accountMenu,
                                 CreatePostCommand createPostCommand,
                                 PostMenu postMenu,
                                 SubredditMenu subredditMenu) {
        MenuDispatcher dispatcher = new MenuDispatcher(output);
        dispatcher.registerCommand("1", new AccountCommand(accountMenu));
        dispatcher.registerCommand("2", new PostCommand(createPostCommand));
        dispatcher.registerCommand("3", new InteractionCommand(postMenu));
        dispatcher.registerCommand("4", new SubredditCommand(subredditMenu));
        dispatcher.registerCommand("5", new LoggerCommand(stringReader, output));
        return dispatcher;
    }
}
