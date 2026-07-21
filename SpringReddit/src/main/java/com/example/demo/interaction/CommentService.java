package com.example.demo.interaction;

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
                .orElseThrow(() -> new RuntimeException("Postarea cu ID-ul " + postId + " nu există!"));

        User author = null;

        Comment newComment = new Comment(text, author, post);
        commentRepository.save(newComment);
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElse(null);
    }

    public void replyToComment(Long postId, Long parentCommentId, String text) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Postarea nu există!"));

        Comment parentComment = commentRepository.findById(parentCommentId)
                .orElseThrow(() -> new RuntimeException("Comentariul părinte nu există!"));

        User author = null;

        Comment reply = new Comment(text, author, post);
        parentComment.addReply(reply);

        commentRepository.save(reply);
    }

    public void editComment(Long postId, Long commentId, String newText) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comentariul nu a fost găsit!"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new RuntimeException("Comentariul nu aparține acestei postări!");
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