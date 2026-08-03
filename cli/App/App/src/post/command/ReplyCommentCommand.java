package post.command;

import api.CommentClient;
import io.StringReader;
import io.OutputWriter;

public class ReplyCommentCommand implements CommentActionCommand {
    private final CommentClient commentClient;
    private final StringReader stringReader;
    private final OutputWriter output;

    public ReplyCommentCommand(CommentClient commentClient, StringReader stringReader, OutputWriter output) {
        this.commentClient = commentClient;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        String text = stringReader.readString("Enter your reply text:");
        commentClient.replyToComment(postId, commentId, text);
        output.write("Reply added successfully.");
    }
}
