package com.example.springreddit.service; // Sau unde ai tu pachetul de servicii

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.repository.AccountRepository;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.CommentVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
    public void vote(Long commentId, Long accountId, int voteDirection) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        Account account = null;
        Optional<CommentVote> existingVoteOpt = commentVoteRepository.findByCommentAndAccount(comment, account);

        if (existingVoteOpt.isPresent()) {
            CommentVote existingVote = existingVoteOpt.get();

            if (existingVote.getVoteDirection() == voteDirection) {
                commentVoteRepository.delete(existingVote);
            } else {
                existingVote.setVoteDirection(voteDirection);
                commentVoteRepository.save(existingVote);
            }
        } else {
            CommentVote newVote = new CommentVote(comment, account, voteDirection);
            commentVoteRepository.save(newVote);
        }
    }
}