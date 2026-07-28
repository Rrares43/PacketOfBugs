package post.service;

import post.model.Comment;
import post.model.CommentVote;
import post.repository.PostRepo;
import logger.Logger;
import logger.LogLevel;
import persistence.DatabaseSync;

import java.util.Optional;

public class CommentVoteServiceImpl implements CommentVoteService {
    private final PostRepo postRepo;
    private final Logger logger;

    public CommentVoteServiceImpl(PostRepo postRepo, Logger logger) {
        this.logger = logger;
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
        Comment comment = postRepo.findCommentById(postId, commentId);

        if (comment == null) {
            logger.log(LogLevel.ERROR, "Comment with id " + commentId + " does not exist in post " + postId);
            return "Error: Comment does not exist.\n";
        }

        String currentUsername = postRepo.getCurrentUser();
        if (currentUsername == null) {
            logger.log(LogLevel.ERROR, "No user is currently logged in.");
            return "Error: No user is currently logged in.\n";
        }

        Optional<CommentVote> existingVoteOpt = comment.getUserVote(currentUsername);
        String voteType = isUpvote ? "Upvote" : "Downvote";

        if (choice == 1) {
            if (existingVoteOpt.isPresent()) {
                CommentVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    return "You have already added an " + voteType + " to this comment.\n";
                } else {
                    existingVote.setUpvote(isUpvote);
                    DatabaseSync.upsertCommentVote(currentUsername, commentId, isUpvote ? 1 : -1);
                    logger.log(LogLevel.INFO, "Vote direction changed for comment " + commentId);
                    postRepo.saveToFile();
                    return voteType + " updated successfully.\n";
                }
            } else {
                comment.getVotes().add(new CommentVote(currentUsername, commentId, isUpvote));
                DatabaseSync.upsertCommentVote(currentUsername, commentId, isUpvote ? 1 : -1);
                logger.log(LogLevel.INFO, "New vote added for comment " + commentId);
                postRepo.saveToFile();
                return voteType + " added successfully.\n";
            }
        }
        else if (choice == 2) {
            if (existingVoteOpt.isPresent()) {
                CommentVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    comment.getVotes().remove(existingVote);
                    DatabaseSync.removeCommentVote(currentUsername, commentId, isUpvote ? 1 : -1);
                    logger.log(LogLevel.INFO, "Vote removed for comment " + commentId);
                    postRepo.saveToFile();
                    return voteType + " removed successfully.\n";
                } else {
                    return "You cannot remove a vote you have not cast.\n";
                }
            } else {
                return "No " + voteType + " exists to remove.\n";
            }
        }

        return "Invalid choice.\n";
    }
}