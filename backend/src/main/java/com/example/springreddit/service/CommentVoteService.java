package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.CommentVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class CommentVoteService {

    private final CommentVoteRepository commentVoteRepository;
    private final CommentRepository commentRepository;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public CommentVoteService(CommentVoteRepository commentVoteRepository,
                              CommentRepository commentRepository) {
        this.commentVoteRepository = commentVoteRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public String vote(UUID postId, UUID commentId, Account account, boolean isUpvote, int choice) {
        validateVoteRequest(postId, commentId, account, choice);
        Comment comment = commentRepository.findByIdAndPost_Id(commentId, postId)
                .orElseThrow(() -> {
                    LOGGER.warn("Vote failed: comment not found with ID: {} on post ID: {}", commentId, postId);
                    return new IllegalArgumentException("Comment does not exist.");
                });
        if (comment.isDeleted()) {
            LOGGER.warn("Vote failed: cannot vote on deleted comment with ID: {}", commentId);
            throw new IllegalArgumentException("Deleted comments cannot be voted on");
        }

        Optional<CommentVote> existingVoteOpt = commentVoteRepository.findByCommentAndAccount(comment, account);
        String voteType = isUpvote ? "Upvote" : "Downvote";

        if (choice == 1) {
            if (existingVoteOpt.isPresent()) {
                CommentVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    LOGGER.info("Vote failed: duplicate {} for comment ID: {} by account ID: {}", voteType, commentId, account.getId());
                    return "You have already added an " + voteType + " to this comment.";
                }
                existingVote.setUpvote(isUpvote);
                commentVoteRepository.save(existingVote);
                LOGGER.info("Vote changed to {} for comment ID: {} by account ID: {}", voteType, commentId, account.getId());
                return voteType + " updated successfully.";
            }
            commentVoteRepository.save(new CommentVote(comment, account, isUpvote ? CommentVote.UPVOTE : CommentVote.DOWNVOTE));
            LOGGER.info("{} added for comment ID: {} by account ID: {}", voteType, commentId, account.getId());
            return voteType + " added successfully.";
        }

        if (choice == 2) {
            if (existingVoteOpt.isPresent()) {
                CommentVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    commentVoteRepository.delete(existingVote);
                    LOGGER.info("{} removed for comment ID: {} by account ID: {}", voteType, commentId, account.getId());
                    return voteType + " removed successfully.";
                }
                LOGGER.info("Vote removal failed: vote type mismatch for comment ID: {} by account ID: {}", commentId, account.getId());
                return "You cannot remove a vote you have not cast.";
            }
            LOGGER.info("Vote removal failed: no existing {} for comment ID: {} by account ID: {}", voteType, commentId, account.getId());
            return "No " + voteType + " exists to remove.";
        }

        return "Invalid choice.";
    }

    public long countUpvotes(UUID commentId) {
        return commentVoteRepository.countByComment_IdAndVoteType(commentId, CommentVote.UPVOTE);
    }

    public long countDownvotes(UUID commentId) {
        return commentVoteRepository.countByComment_IdAndVoteType(commentId, CommentVote.DOWNVOTE);
    }

    public int currentVote(UUID commentId, Long accountId) {
        return commentVoteRepository.findByComment_IdAndAccount_Id(commentId, accountId)
                .map(vote -> (int) vote.getVoteType())
                .orElse(0);
    }

    private void validateVoteRequest(UUID postId, UUID commentId, Account account, int choice) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (choice != 1 && choice != 2) {
            throw new IllegalArgumentException("Choice must be 1 or 2");
        }
    }
}
