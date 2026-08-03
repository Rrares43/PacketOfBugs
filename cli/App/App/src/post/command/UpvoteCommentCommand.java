package post.command;

import api.CommentClient;
import io.StringReader;
import io.OutputWriter;

public class UpvoteCommentCommand implements CommentActionCommand {
    private final CommentClient commentClient;
    private final StringReader stringReader;
    private final OutputWriter output;

    public UpvoteCommentCommand(CommentClient commentClient, StringReader stringReader, OutputWriter output) {
        this.commentClient = commentClient;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        output.write("Select: 1 to ADD Upvote | 2 to REMOVE Upvote\n");
        String choiceStr = stringReader.readString("Enter choice (1-2): ");

        try {
            int voteChoice = Integer.parseInt(choiceStr);
            String resultMessage = commentClient.upvoteComment(postId, commentId, voteChoice);
            output.write(resultMessage);
        } catch (NumberFormatException e) {
            output.write("Invalid choice.\n");
        }
    }
}
