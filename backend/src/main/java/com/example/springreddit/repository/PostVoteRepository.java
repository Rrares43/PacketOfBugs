package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.model.PostVote;
import com.example.springreddit.model.PostVoteId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, PostVoteId> {

    Optional<PostVote> findByPostAndAccount(Post post, Account account);

    @Query("""
            SELECT v.voteType
            FROM PostVote v
            WHERE v.post.id = :postId AND v.account.id = :accountId
            """)
    Optional<Short> findVoteTypeByPostIdAndAccountId(
            @Param("postId") UUID postId,
            @Param("accountId") Long accountId);

    @Query("""
            SELECT v.voteType
            FROM PostVote v
            WHERE v.post.id = :postId AND v.account.username = :username
            """)
    Optional<Short> findVoteTypeByPostIdAndAccountUsername(
            @Param("postId") UUID postId,
            @Param("username") String username);

    long countByPostAndVoteType(Post post, short voteType);

    @Query("""
            SELECT v.voteType, COUNT(v)
            FROM PostVote v
            WHERE v.post.id = :postId
            GROUP BY v.voteType
            """)
    List<Object[]> countGroupedByPostId(@Param("postId") UUID postId);

    @Query("""
            SELECT v.post.id, v.voteType, COUNT(v)
            FROM PostVote v
            WHERE v.post.id IN :postIds
            GROUP BY v.post.id, v.voteType
            """)
    List<Object[]> countGroupedByPostIds(@Param("postIds") Collection<UUID> postIds);

    @Query("""
            SELECT v.post.id, v.voteType
            FROM PostVote v
            WHERE v.post.id IN :postIds AND v.account.id = :accountId
            """)
    List<Object[]> findVoteTypesByPostIdsAndAccountId(
            @Param("postIds") Collection<UUID> postIds,
            @Param("accountId") Long accountId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            INSERT INTO post_votes (account_id, post_id, vote_type)
            VALUES (:accountId, :postId, :voteType)
            """, nativeQuery = true)
    int insertVote(
            @Param("accountId") Long accountId,
            @Param("postId") UUID postId,
            @Param("voteType") short voteType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE PostVote v
            SET v.voteType = :voteType
            WHERE v.post.id = :postId AND v.account.id = :accountId
            """)
    int updateVoteType(
            @Param("postId") UUID postId,
            @Param("accountId") Long accountId,
            @Param("voteType") short voteType);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            DELETE FROM PostVote v
            WHERE v.post.id = :postId AND v.account.id = :accountId
            """)
    int deleteByPostIdAndAccountId(
            @Param("postId") UUID postId,
            @Param("accountId") Long accountId);

}
