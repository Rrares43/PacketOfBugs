package post.command;

import api.CommentClient;
import io.StringReader;
import io.OutputWriter;

public class DownvoteCommentCommand implements CommentActionCommand {
    private final CommentClient commentClient;
    private final StringReader stringReader;
    private final OutputWriter output;

    public DownvoteCommentCommand(CommentClient commentClient, StringReader stringReader, OutputWriter output) {
        this.commentClient = commentClient;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        output.write("Select: 1 to ADD Downvote | 2 to REMOVE Downvote\n");
        String choiceStr = stringReader.readString("Enter choice (1-2): ");

        try {
            int voteChoice = Integer.parseInt(choiceStr);
            String resultMessage = commentClient.downvoteComment(postId, commentId, voteChoice);
            output.write(resultMessage);
        } catch (NumberFormatException e) {
            output.write("Invalid choice.\n");
        }
    }
}
