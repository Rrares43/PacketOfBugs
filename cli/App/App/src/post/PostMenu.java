package post;

public class PostMenu {
    private final PostInteractionController controller;

    public PostMenu(PostInteractionController controller) {
        this.controller = controller;
    }

    public void interactionQuery() {
        controller.startInteraction();
    }
}
