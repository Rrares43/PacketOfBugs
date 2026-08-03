package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.CommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, CommentVoteId> {

    Optional<CommentVote> findByCommentAndAccount(Comment comment, Account account);

    Optional<CommentVote> findByComment_IdAndAccount_Id(UUID commentId, Long accountId);

    long countByComment_IdAndVoteType(UUID commentId, short voteType);

    /* Currently unused but useful methods
    List<CommentVote> findByComment_Id(UUID commentId);

    long countByCommentAndVoteType(Comment comment, short voteType);

    void deleteByCommentAndAccount(Comment comment, Account account);

     */
}
