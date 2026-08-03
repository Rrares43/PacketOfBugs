package post.command;

import api.CommentClient;
import io.OutputWriter;
import io.StringReader;

public class EditCommentCommand implements CommentActionCommand {
    private final CommentClient commentClient;
    private final StringReader stringReader;
    private final OutputWriter output;

    public EditCommentCommand(CommentClient commentClient, StringReader stringReader, OutputWriter output) {
        this.commentClient = commentClient;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        String text = stringReader.readString("Edit comment:");
        commentClient.editComment(postId, commentId, text);
        output.write("Comment edited successfully.");
    }
}
