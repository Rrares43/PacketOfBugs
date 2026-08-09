package com.example.springreddit.repository;

import com.example.springreddit.model.CommentVote;
import com.example.springreddit.model.CommentVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VoteRepository extends JpaRepository<CommentVote, CommentVoteId> {

    Optional<CommentVote> findByComment_IdAndAccount_Id(UUID commentId, Long accountId);

    long countByComment_IdAndVoteType(UUID commentId, short voteType);

    @Query("""
            SELECT v.voteType, COUNT(v)
            FROM CommentVote v
            WHERE v.comment.id = :commentId
            GROUP BY v.voteType
            """)
    List<Object[]> countGroupedByCommentId(@Param("commentId") UUID commentId);

    @Query("""
            SELECT v.comment.id, v.voteType, COUNT(v)
            FROM CommentVote v
            WHERE v.comment.id IN :commentIds
            GROUP BY v.comment.id, v.voteType
            """)
    List<Object[]> countGroupedByCommentIds(@Param("commentIds") Collection<UUID> commentIds);

    @Query("""
            SELECT v.comment.id, v.voteType
            FROM CommentVote v
            WHERE v.comment.id IN :commentIds AND v.account.id = :accountId
            """)
    List<Object[]> findVoteTypesByCommentIdsAndAccountId(
            @Param("commentIds") Collection<UUID> commentIds,
            @Param("accountId") Long accountId);
}
