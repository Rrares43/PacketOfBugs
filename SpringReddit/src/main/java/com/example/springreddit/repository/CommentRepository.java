package com.example.springreddit.repository;

import com.example.springreddit.model.Comment;
import com.example.springreddit.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost(Post post);

    List<Comment> findByPost_Id(Long postId);

    /** Top-level comments (no parent) for a post. */
    List<Comment> findByPost_IdAndParentCommentIsNull(Long postId);

    List<Comment> findByParentComment_Id(Long parentCommentId);

    Optional<Comment> findByIdAndPost_Id(Long id, Long postId);

    void deleteByPost_Id(Long postId);
}
