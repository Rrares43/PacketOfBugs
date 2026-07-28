package post.repository;
import post.PostInteractionController;

public class PostMenu {
    private final PostInteractionController controller;

    public PostMenu(PostInteractionController controller) {
        this.controller = controller;
    }
    public void interactionQuery() {
        controller.startInteraction();
    }
}