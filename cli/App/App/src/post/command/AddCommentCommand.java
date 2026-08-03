package post.command;

import api.CommentClient;
import io.StringReader;
import io.OutputWriter;

public class AddCommentCommand implements PostActionCommand {
    private final CommentClient commentClient;
    private final StringReader stringReader;
    private final OutputWriter output;

    public AddCommentCommand(CommentClient commentClient, StringReader stringReader, OutputWriter output) {
        this.commentClient = commentClient;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId) {
        String text = stringReader.readString("Enter your comment text:");
        commentClient.comment(postId, text);
        output.write("Comment processed.");
    }
}
