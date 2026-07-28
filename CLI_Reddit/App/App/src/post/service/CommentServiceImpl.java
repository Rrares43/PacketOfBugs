package post.service;

import post.model.Comment;
import post.model.Post;
import post.repository.PostRepo;
import logger.Logger;
import logger.LogLevel;

public class CommentServiceImpl implements CommentService {

    private final PostRepo postRepo;
    private final Logger logger;

    public CommentServiceImpl(PostRepo postRepository, Logger logger) {
        this.postRepo = postRepository;
        this.logger = logger;
    }

    @Override
    public void comment(int postId, String text) {
        Post post = postRepo.findPostById(postId);
        if (post == null) {
            logger.log(LogLevel.ERROR, "Post not found for comment: " + postId);
            throw new IllegalArgumentException("Post not found");
        }

        int commentId = postRepo.getNextCommentId();
        Comment newComment = new Comment(commentId, text, postRepo.getCurrentUser());
        post.addComment(newComment);
        postRepo.saveToFile();

        logger.log(LogLevel.INFO, "Comment added ID: " + commentId + " by " + postRepo.getCurrentUser());
    }

    @Override
    public void replyToComment(int postId, int parentCommentId, String text) {
        Post post = postRepo.findPostById(postId);
        if (post == null) {
            logger.log(LogLevel.ERROR, "Post not found for reply: " + postId);
            throw new IllegalArgumentException("Post not found");
        }

        Comment parentComment = postRepo.findCommentById(postId, parentCommentId);
        if (parentComment == null) {
            logger.log(LogLevel.ERROR, "Base comment not found: " + parentCommentId);
            throw new IllegalArgumentException("Base comment not found.");
        }

        int replyId = postRepo.getNextCommentId();
        Comment reply = new Comment(replyId, text, postRepo.getCurrentUser());
        parentComment.addreply(reply);
        postRepo.saveToFile();

        logger.log(LogLevel.INFO, "Reply ID " + replyId + " added at comment " + parentCommentId);
    }

    @Override
    public void editComment(int postId, int commentId, String newText) {
        Post post = postRepo.findPostById(postId);
        if (post == null) {
            logger.log(LogLevel.ERROR, "Post not found for edit: " + postId);
            throw new IllegalArgumentException("Post not found");
        }

        Comment comment = postRepo.findCommentById(postId, commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found.");
        }

        if (!comment.getAuthor().equals(postRepo.getCurrentUser())) {
            logger.log(LogLevel.ERROR, "Comment " + commentId + " cannot be edited by " + postRepo.getCurrentUser());
            throw new SecurityException("Comment cannot be edited");
        }

        comment.setText(newText);
        postRepo.saveToFile();
        logger.log(LogLevel.INFO, "Comment edited: " + commentId);
    }

    @Override
    public void deleteComment(int postId, int commentId) {
        Post post = postRepo.findPostById(postId);
        if (post == null) {
            logger.log(LogLevel.ERROR, "Post not found for delete: " + postId);
            throw new IllegalArgumentException("Post not found");
        }

        Comment comment = postRepo.findCommentById(postId, commentId);
        if (comment == null) {
            throw new IllegalArgumentException("Comment not found.");
        }

        if (!comment.getAuthor().equals(postRepo.getCurrentUser())) {
            logger.log(LogLevel.ERROR, "Comment " + commentId + " cannot be deleted by " + postRepo.getCurrentUser());
            throw new SecurityException("Comment cannot be deleted");
        }

        if (!postRepo.removeComment(postId, commentId)) {
            throw new IllegalArgumentException("Comment not found.");
        }

        postRepo.saveToFile();
        logger.log(LogLevel.INFO, "Comment deleted: " + commentId);
    }

    @Override
    public Comment findCommentById(int commentId) {
        for (Post post : postRepo.findAllPosts()) {
            Comment found = postRepo.findCommentById(post.getId(), commentId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
    @Override
    public void validateReply(int postId, Integer parentCommentId) {
        if (parentCommentId == null || findCommentById(parentCommentId) == null) {
            throw new IllegalArgumentException("Base comment not found.");
        }
    }
}
