package post.command;

import post.service.CommentService;
import io.OutputWriter;

public class DeleteCommentCommand implements CommentActionCommand {
    private final CommentService commentService;
    private final OutputWriter output;

    public DeleteCommentCommand(CommentService commentService, OutputWriter output) {
        this.commentService = commentService;

        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        commentService.deleteComment(postId, commentId);
        output.write("Comment deleted successfully.");
    }
}
