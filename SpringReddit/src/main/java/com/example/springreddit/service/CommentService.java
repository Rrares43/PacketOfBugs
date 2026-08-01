package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public Comment comment(Long postId, String text, Account author) {
        validateText(text);
        validateAuthor(author);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Comment creation failed: post not found with ID: {}", postId);
                    return new IllegalArgumentException("Post not found");
                });

        Comment newComment = new Comment(text, author, post);
        post.addComment(newComment);
        Comment savedComment = commentRepository.save(newComment);
        log.info("Comment created successfully with ID: {} on post ID: {} by author ID: {}", savedComment.getId(), postId, author.getId());
        return savedComment;
    }

    public Comment findCommentById(Long commentId) {
        validateCommentId(commentId);
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    public List<Comment> getTopLevelComments(Long postId) {
        validatePostId(postId);
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }
        return commentRepository.findByPost_IdAndParentCommentIsNullOrderByCreatedAtAscIdAsc(postId);
    }

    @Transactional
    public Comment replyToComment(Long postId, Long parentCommentId, String text, Account author) {
        validateText(text);
        validateAuthor(author);
        validatePostId(postId);
        validateParentCommentId(parentCommentId);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    log.warn("Reply creation failed: post not found with ID: {}", postId);
                    return new IllegalArgumentException("Post not found");
                });

        Comment parentComment = findCommentById(parentCommentId);
        if (!parentComment.belongsToPost(postId)) {
            log.warn("Reply creation failed: base comment not found for parent ID: {} on post ID: {}", parentCommentId, postId);
            throw new IllegalArgumentException("Base comment not found.");
        }
        if (parentComment.isDeleted()) {
            log.warn("Reply creation failed: cannot reply to deleted comment with ID: {}", parentCommentId);
            throw new IllegalArgumentException("Deleted comments cannot be replied to");
        }

        Comment reply = new Comment(text, author, post);
        parentComment.addReply(reply);
        Comment savedReply = commentRepository.save(reply);
        log.info("Reply created successfully with ID: {} to parent comment ID: {} on post ID: {} by author ID: {}", savedReply.getId(), parentCommentId, postId, author.getId());
        return savedReply;
    }

    @Transactional
    public Comment editComment(Long postId, Long commentId, String newText, Account editor) {
        validateText(newText);
        validatePostId(postId);
        validateCommentId(commentId);
        validateAuthor(editor);
        Comment comment = findCommentById(commentId);
        if (!comment.belongsToPost(postId)) {
            log.warn("Comment edit failed: comment not found for ID: {} on post ID: {}", commentId, postId);
            throw new IllegalArgumentException("Comment not found.");
        }
        if (comment.isDeleted()) {
            log.warn("Comment edit failed: cannot edit deleted comment with ID: {}", commentId);
            throw new IllegalArgumentException("Deleted comments cannot be edited");
        }
        if (!comment.isAuthoredBy(editor)) {
            log.warn("Comment edit failed: unauthorized access to comment ID: {} by account ID: {}", commentId, editor.getId());
            throw new SecurityException("Comment cannot be edited");
        }

        comment.editContent(newText);
        commentRepository.save(comment);
        log.info("Comment edited successfully with ID: {} on post ID: {} by editor ID: {}", commentId, postId, editor.getId());
        return comment;
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Account deleter) {
        validatePostId(postId);
        validateCommentId(commentId);
        validateAuthor(deleter);
        Comment comment = findCommentById(commentId);
        if (!comment.belongsToPost(postId)) {
            log.warn("Comment delete failed: comment not found for ID: {} on post ID: {}", commentId, postId);
            throw new IllegalArgumentException("Comment not found.");
        }
        if (comment.isDeleted()) {
            log.warn("Comment delete failed: comment with ID: {} is already deleted", commentId);
            throw new IllegalArgumentException("Comment is already deleted");
        }
        if (!comment.isAuthoredBy(deleter)) {
            log.warn("Comment delete failed: unauthorized access to comment ID: {} by account ID: {}", commentId, deleter.getId());
            throw new SecurityException("Comment cannot be deleted");
        }

        comment.softDelete();
        commentRepository.save(comment);
        log.info("Comment deleted successfully with ID: {} on post ID: {} by deleter ID: {}", commentId, postId, deleter.getId());
    }

    private void validateText(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be blank");
        }
        if (text.length() > 3000) {
            throw new IllegalArgumentException("Comment content must not exceed 3000 characters");
        }
    }

    private void validateAuthor(Account author) {
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }
    }

    private void validatePostId(Long postId) {
        if (postId == null) {
            throw new IllegalArgumentException("Post ID cannot be null");
        }
    }

    private void validateParentCommentId(Long parentCommentId) {
        if (parentCommentId == null) {
            throw new IllegalArgumentException("Parent comment ID cannot be null");
        }
    }

    private void validateCommentId(Long commentId) {
        if (commentId == null) {
            throw new IllegalArgumentException("Comment ID cannot be null");
        }
    }
}
