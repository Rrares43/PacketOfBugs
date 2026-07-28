package menu;

import post.repository.PostMenu;

public class InterractionCommand implements MenuCommand {
    private final PostMenu postMenu;

    public InterractionCommand(PostMenu postMenu) {
        this.postMenu = postMenu;
    }
    @Override
    public void execute(){
        postMenu.interactionQuery();
    }
}
