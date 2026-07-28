package post.service;

import persistence.RedditApiClient;
import post.repository.PostRepo;

public class CommentVoteServiceImpl implements CommentVoteService {
    private final PostRepo postRepo;

    public CommentVoteServiceImpl(PostRepo postRepo) {
        this.postRepo = postRepo;
    }

    @Override
    public String upvoteComment(int postId, int commentId, int choice) {
        return handleVote(postId, commentId, true, choice);
    }

    @Override
    public String downvoteComment(int postId, int commentId, int choice) {
        return handleVote(postId, commentId, false, choice);
    }

    private String handleVote(int postId, int commentId, boolean isUpvote, int choice) {
        Long accountId = postRepo.getCurrentAccountId();
        if (accountId == null) {
            return "Error: No user is currently logged in.\n";
        }

        try {
            String message = RedditApiClient.voteComment(postId, commentId, accountId, isUpvote, choice);
            return message + "\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }
}
