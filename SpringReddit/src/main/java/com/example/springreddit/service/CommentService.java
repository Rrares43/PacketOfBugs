package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.model.Comment;
import com.example.springreddit.model.Post;
import com.example.springreddit.repository.CommentRepository;
import com.example.springreddit.repository.PostRepository;
import com.example.springreddit.repository.AccountRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    public void comment(Long postId, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post with ID-ul " + postId + " not found"));

        Account author = null;

        Comment newComment = new Comment(text, author, post);
        commentRepository.save(newComment);
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    public void replyToComment(Long postId, Long parentCommentId, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Original comment not found"));

        Account author = null;

        Comment reply = new Comment(text, author, post);
        parentComment.addReply(reply);

        commentRepository.save(reply);
    }

    public void editComment(Long postId, Long commentId, String newText) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Comment does not belong to this post");
        }

        comment.setContent(newText);
        commentRepository.save(comment);
    }

    public void deleteComment(Long postId, Long commentId) {
        if (commentRepository.existsById(commentId)) {
            commentRepository.deleteById(commentId);
        }
    }
}