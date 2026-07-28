package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.CommentVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Mirrors CLI {@code CommentVoteServiceImpl} vote semantics (choice 1 = add/change, 2 = remove).
 */
@Service
public class CommentVoteService {

    private final CommentVoteRepository commentVoteRepository;
    private final CommentRepository commentRepository;

    public CommentVoteService(CommentVoteRepository commentVoteRepository,
                              CommentRepository commentRepository) {
        this.commentVoteRepository = commentVoteRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public String vote(Long postId, Long commentId, Account account, boolean isUpvote, int choice) {
        Comment comment = commentRepository.findByIdAndPost_Id(commentId, postId)
                .orElseThrow(() -> new IllegalArgumentException("Comment does not exist."));

        Optional<CommentVote> existingVoteOpt = commentVoteRepository.findByCommentAndAccount(comment, account);
        String voteType = isUpvote ? "Upvote" : "Downvote";

        if (choice == 1) {
            if (existingVoteOpt.isPresent()) {
                CommentVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    return "You have already added an " + voteType + " to this comment.";
                }
                existingVote.setUpvote(isUpvote);
                commentVoteRepository.save(existingVote);
                return voteType + " updated successfully.";
            }
            commentVoteRepository.save(new CommentVote(comment, account, isUpvote ? CommentVote.UPVOTE : CommentVote.DOWNVOTE));
            return voteType + " added successfully.";
        }

        if (choice == 2) {
            if (existingVoteOpt.isPresent()) {
                CommentVote existingVote = existingVoteOpt.get();
                if (existingVote.isUpvote() == isUpvote) {
                    commentVoteRepository.delete(existingVote);
                    return voteType + " removed successfully.";
                }
                return "You cannot remove a vote you have not cast.";
            }
            return "No " + voteType + " exists to remove.";
        }

        return "Invalid choice.";
    }

    public long countUpvotes(Long commentId) {
        return commentVoteRepository.countByComment_IdAndVoteType(commentId, CommentVote.UPVOTE);
    }

    public long countDownvotes(Long commentId) {
        return commentVoteRepository.countByComment_IdAndVoteType(commentId, CommentVote.DOWNVOTE);
    }
}
