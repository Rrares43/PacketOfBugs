package post.command;

import post.service.CommentService;
import io.StringReader;
import io.OutputWriter;

public class ReplyCommentCommand implements CommentActionCommand {
    private final CommentService commentService;
    private final StringReader stringReader;
    private final OutputWriter output;

    public ReplyCommentCommand(CommentService commentService, StringReader stringReader, OutputWriter output) {
        this.commentService = commentService;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {

        commentService.validateReply(postId, commentId);
        String text = stringReader.readString("Enter your reply text:");
        commentService.replyToComment(postId, commentId, text);
        output.write("Reply added successfully.");
    }

}