package post.command;

import account.SessionService;
import post.PostView;
import post.model.Post;
import post.service.PostService;

public class CreatePostCommand {
    private final PostView view;
    private final PostService postService;
    private final SessionService sessionService;

    public CreatePostCommand(PostView view, PostService postService, SessionService sessionService) {
        this.view = view;
        this.postService = postService;
        this.sessionService = sessionService;
    }

    public void execute() {
        if (!sessionService.isLoggedIn()) {
            System.out.println("You must be logged in to create a post.");
            return;
        }

        System.out.println("CREATE A NEW POST (or enter 0 in any field to return to the previous menu):");
        String title = view.askForTitle();
        if (title.equals("0")) {
            return;
        }
        String subreddit = view.askForSubreddit();
        if (subreddit.equals("0")) {
            return;
        }
        String baseContent = view.askForContent();
        if (baseContent.equals("0")) {
            return;
        }
        String attachment = view.askForAttachment();
        String finalContent = baseContent + attachment;

        try {
            Post myNewPost = postService.createPost(
                    sessionService.getCurrentUsername(),
                    title,
                    finalContent,
                    subreddit
            );
            System.out.println("Post created successfully!");
            view.displayPost(myNewPost);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
