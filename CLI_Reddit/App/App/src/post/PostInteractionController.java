package post;

import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;
import post.model.Comment;
import post.model.Post;
import post.repository.PostRepository;
import post.service.CommentService;
import post.service.CommentVoteService;
import post.command.CommentActionCommand;
import post.command.PostActionCommand;
import io.IntReader;
import io.OutputWriter;
import io.StringReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostInteractionController {
    private final StringReader stringReader;
    private final IntReader intReader;
    private final OutputWriter output;
    private final PostView postView;
    private final CommentService commentService;
    private final PostRepository postRepo;
    private final CommentVoteService commentVoteService;
    private final Map<String, PostActionCommand> postCommands = new HashMap<>();
    private final Map<String, CommentActionCommand> commentCommands = new HashMap<>();

    public void registerPostCommand(String choice, PostActionCommand command) {
        postCommands.put(choice, command);
    }

    public void registerCommentCommand(String choice, CommentActionCommand command) {
        commentCommands.put(choice, command);
    }

    public PostInteractionController(StringReader stringReader, IntReader intReader,
                                     OutputWriter output, PostView postView,
                                     CommentService commentService, CommentVoteService commentVoteService,
                                     PostRepository postRepo) {
        this.stringReader = stringReader;
        this.intReader = intReader;
        this.output = output;
        this.postView = postView;
        this.commentService = commentService;
        this.commentVoteService = commentVoteService;
        this.postRepo = postRepo;
    }

    public void startInteraction() {
        String subredditName = askForSubreddit();
        if (subredditName == null) {
            return;
        }

        List<Post> posts = postRepo.findPostsBySubreddit(subredditName);
        if (posts.isEmpty()) {
            output.write("No posts found in " + subredditName + ".");
            return;
        }

        output.write("\nPosts in " + subredditName + ":");
        for (Post post : posts) {
            output.write("ID: " + post.getId() + " | Title: " + post.getTitle());
        }

        int postID = intReader.readInt("Enter the ID of the post you want to interact with:");
        Post foundPost = null;
        for (Post post : posts) {
            if (post.getId() == postID) {
                foundPost = post;
                break;
            }
        }

        if (foundPost == null) {
            output.write("Error: Post with ID " + postID + " does not exist in " + subredditName + ".");
            return;
        }
        handlePostMenu(postID, subredditName);
    }

    private String askForSubreddit() {
        List<Subreddit> subreddits = SubredditRepository.loadSubreddits();
        if (subreddits.isEmpty()) {
            output.write("No subreddits available.");
            return null;
        }

        while (true) {
            output.write("\nWhich subreddit would you like to browse?");
            output.write("Available subreddits:");
            for (Subreddit subreddit : subreddits) {
                output.write("- " + subreddit.getName() + " (Owner: " + subreddit.getOwner() + ")" + " Number of posts: " + findNrOfPostsinSubreddit(subreddit.getName()));
            }

            String input = stringReader.readString("Enter subreddit name (or 0 to cancel): ");
            if ("0".equals(input)) {
                output.write("Action cancelled.");
                return null;
            }

            String normalized = normalizeSubredditName(input);
            for (Subreddit subreddit : subreddits) {
                if (subreddit.getName().equals(normalized)) {
                    return subreddit.getName();
                }
            }

            output.write("Invalid subreddit. Please try again.");
        }
    }

    private String normalizeSubredditName(String name) {
        if (name == null) {
            return "";
        }
        if (!name.startsWith("r/")) {
            return "r/" + name;
        }
        return name;
    }

    private void handlePostMenu(int postID, String subredditName) {
        while (true) {
            Post currentPost = null;
            List<Post> posts = postRepo.findPostsBySubreddit(subredditName);
            for (Post p : posts) {
                if (p.getId() == postID) {
                    currentPost = p;
                    break;
                }
            }

            if (currentPost == null) {
                output.write("Error: Post no longer exists.");
                break;
            }

            output.write("\n==========================================");
            postView.displayPost(currentPost);
            output.write("==========================================\n");
            output.write("Choose an action:\n1. Upvote\n2. Downvote\n3. Add Comment\n4. Edit Post\n5. Delete Post\n6. Interact with a specific Comment\n0. Go Back");
            String choice = stringReader.readString("Select option (0-6): ");

            if (choice.equals("0")) {
                output.write("Returning to subreddit...");
                break;
            }

            PostActionCommand command = postCommands.get(choice);
            if (command != null) {
                try {
                    command.execute(postID);
                } catch (Exception e) {
                    output.write("Error: " + e.getMessage());
                }
            } else {
                output.write("Invalid choice! Please try again.");
            }
        }
    }

    public void manageCommentInteraction(int postID) {
        int commentID = intReader.readInt("Insert Comment ID to interact with:");
        Comment foundComment = commentService.findCommentById(commentID);
        if (foundComment == null) {
            output.write("Error: Comment does not exist.");
            return;
        }

        if (foundComment.getPostId() != postID) {
            output.write("Error: Base comment not found!");
            return;
        }

        output.write("Selected comment by: " + foundComment.getAuthor());

        output.write("1. Upvote comment\n2. Downvote comment\n3. Reply to comment\n4. Edit comment\n5. Delete comment");
        String commentChoice = stringReader.readString("Select option (1-5): ");

        CommentActionCommand command = commentCommands.get(commentChoice);
        if (command != null) {
            try {
                command.execute(postID, commentID);
            } catch (Exception e) {
                output.write("Error: " + e.getMessage());
            }
        } else {
            output.write("Invalid choice! Action cancelled.");
        }
    }

    private int findNrOfPostsinSubreddit(String subredditName){
        List<Post> posts = postRepo.findPostsBySubreddit(subredditName);
        return posts.size();
    }
}