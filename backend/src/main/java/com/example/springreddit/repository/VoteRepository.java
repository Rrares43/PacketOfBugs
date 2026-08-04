package com.example.springreddit.repository;

import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.CommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<CommentVote, CommentVoteId> {

    Optional<CommentVote> findByComment_IdAndAccount_Id(UUID commentId, Long accountId);

    long countByComment_IdAndVoteType(UUID commentId, short voteType);

    void deleteByComment_IdAndAccount_Id(UUID commentId, Long accountId);
}
