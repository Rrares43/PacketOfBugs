package post.command;

import account.SessionService;
import post.model.Post;
import post.service.PostService;
import post.PostView;

import java.util.Scanner;

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
        boolean running = true;
        Scanner scanner = new Scanner(System.in);
        while (running) {
            System.out.println("Do you want to create a new post? (y/n)");
            String answer = scanner.nextLine().toLowerCase();
            if (answer.equals("n")) {
                running = false;
            } else if (!answer.equals("y")) {
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
                return;
            }
            if (running) {
                System.out.println("CREATE A NEW POST (or enter 0 in any field to return to the previous menu):");
                String title = view.askForTitle();
                if (title.equals("0")) continue;
                String subreddit = view.askForSubreddit();
                if (subreddit.equals("0")) continue;
                String baseContent = view.askForContent();
                if (baseContent.equals("0")) continue;
                String attachment = view.askForAttachment();

                String finalContent = baseContent + attachment;

                Post myNewPost = postService.createPost(
                        sessionService.getCurrentUsername(),
                        title,
                        finalContent,
                        subreddit
                );

                view.displayPost(myNewPost);
            }
        }
    }
}
