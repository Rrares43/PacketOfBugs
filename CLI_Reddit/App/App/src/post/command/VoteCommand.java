package post.command;

import post.service.PostVoteService;
import io.IntReader;
import io.OutputWriter;

public class VoteCommand implements PostActionCommand {
    private final PostVoteService postVoteService;
    private final IntReader intReader;
    private final OutputWriter output;
    private final boolean isUpvote;

    public VoteCommand(PostVoteService postVoteService, IntReader intReader, OutputWriter output, boolean isUpvote) {
        this.postVoteService = postVoteService;
        this.intReader = intReader;
        this.output = output;
        this.isUpvote = isUpvote;
    }

    @Override
    public void execute(int postId) {
        String voteType = isUpvote ? "Upvote" : "Downvote";
        output.write("Select: 1 to ADD " + voteType + " | 2 to REMOVE " + voteType + "\n");

        try {
            int choice = intReader.readInt("Enter choice (1-2): ");

            String resultMessage;
            if (isUpvote) {
                resultMessage = postVoteService.upvote(postId, choice);
            } else {
                resultMessage = postVoteService.downvote(postId, choice);
            }
            output.write(resultMessage);

        } catch (Exception e) {
            output.write("Invalid choice.\n");
        }
    }
}