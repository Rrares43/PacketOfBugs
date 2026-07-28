package post;
import io.TextFormatter;
import post.model.Comment;
import post.model.Post;
import post.attachment.AttachmentHandler;
import post.model.PostVote;
import post.validator.Validator;
import io.OutputWriter;
import io.StringReader;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PostView {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final Validator<String> notBlankValidator;

    private final Validator<String> titleLengthValidator;
    private final Validator<String> contentLengthValidator;
    private final Map<String, AttachmentHandler> attachmentHandlers;

    public PostView(StringReader stringReader, OutputWriter output,
                    Validator<String> notBlankValidator,
                    Validator<String> titleLengthValidator,
                    Validator<String> contentLengthValidator,
                    Map<String, AttachmentHandler> attachmentHandlers) {
        this.stringReader = stringReader;
        this.output = output;
        this.notBlankValidator = notBlankValidator;
        this.titleLengthValidator = titleLengthValidator;
        this.contentLengthValidator = contentLengthValidator;
        this.attachmentHandlers = attachmentHandlers;
    }

    public String askForTitle() {
        while (true) {
            String title = stringReader.readString("Enter post title:");
            if (!notBlankValidator.isValid(title)) {
                output.write("Error: Title cannot be empty!");
            } else if (!titleLengthValidator.isValid(title)) {
                output.write("Error: Title exceeds the maximum allowed length!");
            }
            else if (title.equals("0")){
                output.write("Back to menu");
                return "0";
            }
            else {
                return title;
            }
        }
    }

    public String askForContent() {
        while (true) {
            String content = stringReader.readString("Enter post content:");
            if (!notBlankValidator.isValid(content)) {
                output.write("Error: Content cannot be empty!");
            } else if (!contentLengthValidator.isValid(content)) {
                output.write("Error: Content exceeds the maximum allowed length!");
            }
            else if (content.equals("0")){
                output.write("Back to menu");
                return "0";
            }
            else {
                return content;
            }
        }
    }

    public String askForAttachment() {
        String options = String.join("/", attachmentHandlers.keySet());

        while (true) {
            String type = stringReader.readString("Do you want to add an attachment? (" + options + ")");

            AttachmentHandler handler = attachmentHandlers.get(type.toLowerCase());
            if (handler != null) {
                return handler.handle();
            }
            output.write("Invalid Input. Please type one of: " + options);
        }
    }

    public String askForSubreddit() {
        while (true) {
            String subreddit = stringReader.readString("Enter the subreddit name:");
            List<Subreddit> subreddits = SubredditRepository.loadSubreddits();
            String foundSubreddit = "";
            if (notBlankValidator.isValid(subreddit) && !subreddit.equals("0")){
                for(Subreddit s : subreddits){
                    if(s.getName().equals(subreddit)){
                         foundSubreddit = s.getName();
                    }
                }
            }
            else if (subreddit.equals("0")){
                output.write("Back to menu");
                return "0";
            }
            else if(subreddit.isEmpty()) {
                output.write("Error: Subreddit name cannot be empty!");
            }

            if(foundSubreddit.isEmpty()){
                output.write("Error: Subreddit not found!");
            }
            else{
                return foundSubreddit;
            }
        }
    }

    public void displayPost(Post post) {
        output.write(TextFormatter.header("[" + post.getSubredditName() + "] " + post.getTitle()));
        output.write("By: " + post.getAuthor() + " | ID: " + post.getId());
        output.write(TextFormatter.success("▲ Upvotes: " + post.getUpvotes()) + " | " + TextFormatter.error("▼ Downvotes: " + post.getDownvotes()));
        output.write(TextFormatter.separator(42));
        output.write(post.getContent());
        output.write(TextFormatter.separator(42));
        output.write(TextFormatter.header("COMMENTS:"));

        if (post.getComments().isEmpty()) {
            output.write("No comments yet. Be the first to share your thoughts!");
        } else {
            for (Comment c : post.getComments()) {
                displayCommentTree(c, "   ");
            }
        }
    }

    private void displayCommentTree(Comment comment, String indent) {
        output.write(indent + "↳ " + comment.toString());
        for (Comment reply : comment.getReplies()) {
            displayCommentTree(reply, indent + "      ");
        }
    }
}