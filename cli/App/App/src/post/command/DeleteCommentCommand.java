package post.command;

import api.CommentClient;
import io.OutputWriter;

public class DeleteCommentCommand implements CommentActionCommand {
    private final CommentClient commentClient;
    private final OutputWriter output;

    public DeleteCommentCommand(CommentClient commentClient, OutputWriter output) {
        this.commentClient = commentClient;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        commentClient.deleteComment(postId, commentId);
        output.write("Comment deleted successfully.");
    }
}
