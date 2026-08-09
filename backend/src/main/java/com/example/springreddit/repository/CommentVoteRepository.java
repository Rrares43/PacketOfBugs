package com.example.springreddit.repository;

import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.CommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, CommentVoteId> {

    Optional<CommentVote> findByComment_IdAndAccount_Id(UUID commentId, Long accountId);

    long countByComment_IdAndVoteType(UUID commentId, short voteType);

    /* Currently unused but useful methods
    List<CommentVote> findByComment_Id(UUID commentId);

    void deleteByCommentAndAccount(Comment comment, Account account);

     */
}
