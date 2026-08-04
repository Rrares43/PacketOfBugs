package com.example.springreddit.repository;

import com.example.springreddit.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    @Query("select distinct comment from Comment comment "
            + "left join fetch comment.replies where comment.id = :id")
    Optional<Comment> findByIdWithReplies(@Param("id") UUID id);

    List<Comment> findByPost_IdAndParentCommentIsNullOrderByCreatedAtAscIdAsc(UUID postId);

    Optional<Comment> findByIdAndPost_Id(UUID id, UUID postId);

    long countByPost_Id(UUID postId);
}
