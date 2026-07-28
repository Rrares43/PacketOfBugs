package post.service;

import post.model.Post;
import post.model.PostVote;
import post.repository.PostRepo;
import logger.Logger;
import logger.LogLevel;
import persistence.DatabaseSync;

import java.util.Optional;

public class PostVoteServiceImpl implements PostVoteService {
    private final PostRepo postRepo;
    private final Logger logger;

    public PostVoteServiceImpl(PostRepo postRepo, Logger logger) {
        this.logger = logger;
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
        Post post = postRepo.findPostById(postId);
        if (post == null) {
            logger.log(LogLevel.ERROR, "Post with id " + postId + " does not exist");
            return "Error: Post with ID " + postId + " does not exist.\n";
        }

        String currentUsername = postRepo.getCurrentUser();
        if (currentUsername == null) {
            logger.log(LogLevel.ERROR, "No user is currently logged in.");
            return "Error: No user is currently logged in.\n";
        }

        Optional<PostVote> existingVoteOpt = post.getUserVote(currentUsername);
        String voteTypeStr = isUpvote ? "upvote" : "downvote";

        // Dacă utilizatorul a ales 1 (ADD)
        if (choice == 1) {
            if (existingVoteOpt.isPresent()) {
                PostVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    return "You have already voted! You cannot " + voteTypeStr + " twice.\n";
                } else {
                    existingVote.setUpvote(isUpvote);
                    DatabaseSync.upsertPostVote(currentUsername, postId, isUpvote ? 1 : -1);
                    logger.log(LogLevel.INFO, "Vote direction changed for post " + postId);
                    postRepo.saveToFile();
                    return "Vote changed to " + voteTypeStr + " successfully.\n";
                }
            } else {
                post.getVotes().add(new PostVote(currentUsername, postId, isUpvote));
                DatabaseSync.upsertPostVote(currentUsername, postId, isUpvote ? 1 : -1);
                logger.log(LogLevel.INFO, "New vote added for post " + postId);
                postRepo.saveToFile();
                return voteTypeStr.substring(0, 1).toUpperCase() + voteTypeStr.substring(1) + " added successfully.\n";
            }
        }
        else if (choice == 2) {
            if (existingVoteOpt.isEmpty()) {
                return "Error: You have not voted on this post, so you cannot remove a vote\n";
            }

            PostVote existingVote = existingVoteOpt.get();
            if (existingVote.isUpvote() != isUpvote) {
                return "Error: You are trying to remove an " + voteTypeStr + ", but you cast the opposite vote\n";
            }

            post.getVotes().remove(existingVote);
            DatabaseSync.removePostVote(currentUsername, postId, isUpvote ? 1 : -1);
            logger.log(LogLevel.INFO, "Vote removed for post " + postId);
            postRepo.saveToFile();
            return voteTypeStr.substring(0, 1).toUpperCase() + voteTypeStr.substring(1) + " removed successfully\n";
        }

        return "Invalid choice\n";
    }

    public String getVoteStatus(int postId) {
        Post post = postRepo.findPostById(postId);
        String currentUsername = postRepo.getCurrentUser();

        if (post == null || currentUsername == null) return "";

        Optional<PostVote> vote = post.getUserVote(currentUsername);
        if (vote.isEmpty()) {
            return "[ You have not voted on this post ]";
        }
        return vote.get().isUpvote() ? "[ Status: UPVOTED ]" : "[ Status: DOWNVOTED ]";
    }
}