package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment newComment = new Comment(text, author, post);
        post.addComment(newComment);
        return commentRepository.save(newComment);
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found"));
    }

    public List<Comment> getTopLevelComments(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }
        return commentRepository.findByPost_IdAndParentCommentIsNull(postId);
    }

    @Transactional
    public Comment replyToComment(Long postId, Long parentCommentId, String text, Account author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        Comment parentComment = findCommentById(parentCommentId);
        if (!parentComment.belongsToPost(postId)) {
            throw new IllegalArgumentException("Base comment not found.");
        }

        Comment reply = new Comment(text, author, post);
        parentComment.addReply(reply);
        return commentRepository.save(reply);
    }

    @Transactional
    public Comment editComment(Long postId, Long commentId, String newText, Account editor) {
        Comment comment = findCommentById(commentId);
        if (!comment.belongsToPost(postId)) {
            throw new IllegalArgumentException("Comment not found.");
        }
        if (!comment.isAuthoredBy(editor)) {
            throw new SecurityException("Comment cannot be edited");
        }

        comment.editContent(newText);
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Account deleter) {
        Comment comment = findCommentById(commentId);
        if (!comment.belongsToPost(postId)) {
            throw new IllegalArgumentException("Comment not found.");
        }
        if (!comment.isAuthoredBy(deleter)) {
            throw new SecurityException("Comment cannot be deleted");
        }

        commentRepository.delete(comment);
    }
}
