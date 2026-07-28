package post.service;

import persistence.RedditApiClient;
import post.repository.PostRepo;

public class PostVoteServiceImpl implements PostVoteService {
    private final PostRepo postRepo;

    public PostVoteServiceImpl(PostRepo postRepo) {
        this.postRepo = postRepo;
    }

    @Override
    public String upvote(int postId, int choice) {
        return handleVote(postId, choice, true);
    }

    @Override
    public String downvote(int postId, int choice) {
        return handleVote(postId, choice, false);
    }

    private String handleVote(int postId, int choice, boolean isUpvote) {
        Long accountId = postRepo.getCurrentAccountId();
        if (accountId == null) {
            return "Error: No user is currently logged in.\n";
        }

        try {
            String message = RedditApiClient.votePost(postId, accountId, isUpvote, choice);
            return message + "\n";
        } catch (Exception e) {
            return "Error: " + e.getMessage() + "\n";
        }
    }

    public String getVoteStatus(int postId) {
        return "";
    }
}
