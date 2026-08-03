package menu;

import post.PostMenu;

public class InteractionCommand implements MenuCommand {
    private final PostMenu postMenu;

    public InteractionCommand(PostMenu postMenu) {
        this.postMenu = postMenu;
    }
    @Override
    public void execute(){
        postMenu.interactionQuery();
    }
}
