package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Comment newComment = new Comment(text, author, post);
        post.addComment(newComment);
        return commentRepository.save(newComment);
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with id: " + commentId));
    }

    @Transactional
    public Comment replyToComment(Long postId, Long parentCommentId, String text, Account author) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Comment parentComment = findCommentById(parentCommentId);
        if (!parentComment.belongsToPost(postId)) {
            throw new IllegalArgumentException("Parent comment does not belong to this post");
        }

        Comment reply = new Comment(text, author, post);
        parentComment.addReply(reply);
        return commentRepository.save(reply);
    }

    @Transactional
    public void editComment(Long postId, Long commentId, String newText) {
        Comment comment = findCommentById(commentId);
        if (!comment.belongsToPost(postId)) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }

        comment.editContent(newText);
        commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = findCommentById(commentId);
        if (!comment.belongsToPost(postId)) {
            throw new IllegalArgumentException("Comment does not belong to this post");
        }

        commentRepository.delete(comment);
    }
}
