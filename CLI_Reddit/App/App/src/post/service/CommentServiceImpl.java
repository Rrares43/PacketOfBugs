package post.service;

import persistence.RedditApiClient;
import post.model.Comment;
import post.model.Post;
import post.repository.PostRepo;

public class CommentServiceImpl implements CommentService {

    private final PostRepo postRepo;

    public CommentServiceImpl(PostRepo postRepository) {
        this.postRepo = postRepository;
    }

    @Override
    public void comment(int postId, String text) {
        Long authorId = postRepo.getCurrentAccountId();
        if (authorId == null) {
            throw new IllegalStateException("You must be logged in to comment.");
        }
        RedditApiClient.addComment(postId, text, authorId);
    }

    @Override
    public void replyToComment(int postId, int parentCommentId, String text) {
        Long authorId = postRepo.getCurrentAccountId();
        if (authorId == null) {
            throw new IllegalStateException("You must be logged in to reply.");
        }
        RedditApiClient.replyToComment(postId, parentCommentId, text, authorId);
    }

    @Override
    public void editComment(int postId, int commentId, String newText) {
        Long accountId = postRepo.getCurrentAccountId();
        if (accountId == null) {
            throw new IllegalStateException("You must be logged in to edit a comment.");
        }
        RedditApiClient.editComment(postId, commentId, newText, accountId);
    }

    @Override
    public void deleteComment(int postId, int commentId) {
        Long accountId = postRepo.getCurrentAccountId();
        if (accountId == null) {
            throw new IllegalStateException("You must be logged in to delete a comment.");
        }
        RedditApiClient.deleteComment(postId, commentId, accountId);
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
        if (parentCommentId == null || postRepo.findCommentById(postId, parentCommentId) == null) {
            throw new IllegalArgumentException("Base comment not found.");
        }
    }
}
