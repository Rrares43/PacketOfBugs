package com.example.springreddit.repository;

import com.example.springreddit.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("""
            SELECT DISTINCT comment FROM Comment comment
            LEFT JOIN FETCH comment.author
            LEFT JOIN FETCH comment.post
            LEFT JOIN FETCH comment.replies reply
            LEFT JOIN FETCH reply.author
            WHERE comment.id = :id
            """)
    Optional<Comment> findByIdWithReplies(@Param("id") UUID id);

    @Query("""
            SELECT DISTINCT c FROM Comment c
            LEFT JOIN FETCH c.author
            LEFT JOIN FETCH c.post
            LEFT JOIN FETCH c.parentComment
            WHERE c.post.id = :postId
            ORDER BY c.createdAt ASC, c.id ASC
            """)
    List<Comment> findAllByPostIdWithDetails(@Param("postId") UUID postId);

    Optional<Comment> findByIdAndPost_Id(UUID id, UUID postId);

    long countByPost_Id(UUID postId);

    @Query("""
            SELECT c.post.id, COUNT(c)
            FROM Comment c
            WHERE c.post.id IN :postIds
            GROUP BY c.post.id
            """)
    List<Object[]> countGroupedByPostIds(@Param("postIds") Collection<UUID> postIds);
}
