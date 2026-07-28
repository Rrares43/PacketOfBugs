package menu;

import subreddit.SubredditMenu;

public class SubredditCommand implements MenuCommand {
    private final SubredditMenu subredditMenu;
    public SubredditCommand(SubredditMenu subredditMenu){
        this.subredditMenu = subredditMenu;
    }
    @Override
    public void execute(){
        subredditMenu.startSubredditMenu();
    }
}
