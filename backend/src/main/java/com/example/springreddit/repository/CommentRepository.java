package com.example.springreddit.repository;

import com.example.springreddit.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {
    List<Comment> findByPost_IdAndParentCommentIsNullOrderByCreatedAtAscIdAsc(UUID postId);

    Optional<Comment> findByIdAndPost_Id(UUID id, UUID postId);

    long countByPost_Id(UUID postId);
}
