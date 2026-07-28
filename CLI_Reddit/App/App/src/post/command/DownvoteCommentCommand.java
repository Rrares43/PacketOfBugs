package post.command;

import post.service.CommentVoteService;
import io.StringReader;
import io.OutputWriter;

public class DownvoteCommentCommand implements CommentActionCommand {
    private final CommentVoteService commentVoteService;
    private final StringReader stringReader;
    private final OutputWriter output;

    public DownvoteCommentCommand(CommentVoteService commentVoteService, StringReader stringReader, OutputWriter output) {
        this.commentVoteService = commentVoteService;
        this.stringReader = stringReader;
        this.output = output;
    }

    @Override
    public void execute(int postId, int commentId) {
        output.write("Select: 1 to ADD Downvote | 2 to REMOVE Downvote\n");
        String choiceStr = stringReader.readString("Enter choice (1-2): ");

        try {
            int voteChoice = Integer.parseInt(choiceStr);
            String resultMessage = commentVoteService.downvoteComment(postId, commentId, voteChoice);
            output.write(resultMessage);
        } catch (NumberFormatException e) {
            output.write("Invalid choice.\n");
        }
    }
}