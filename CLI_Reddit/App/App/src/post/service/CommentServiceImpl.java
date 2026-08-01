package post.service;

import account.SessionService;
import persistence.RedditApiClient;
import post.model.Comment;
import post.model.Post;
import post.repository.PostRepo;

public class CommentServiceImpl implements CommentService {

    private final SessionService sessionService;

    public CommentServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void comment(int postId, String text) {
        if(!sessionService.isLoggedIn()){
            throw new SecurityException("You must be logged in to comment.");
        }
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.addComment(postId, text, authorId);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error commenting: " + e.getMessage());
        }
    }

    @Override
    public void replyToComment(int postId, int parentCommentId, String text) {
        if(!sessionService.isLoggedIn()){
            throw new SecurityException("You must be logged in to reply.");
        }
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.replyToComment(postId, parentCommentId, text, authorId);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error replying: " + e.getMessage());
        }
    }

    @Override
    public void editComment(int postId, int commentId, String newText) {
        if(!sessionService.isLoggedIn()){
            throw new SecurityException("You must be logged in to edit a comment.");
        }
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.editComment(postId, commentId, newText, authorId);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error editing comment: " + e.getMessage());
        }
    }

    @Override
    public void deleteComment(int postId, int commentId) {
        if(!sessionService.isLoggedIn()){
            throw new SecurityException("You must be logged in to delete a comment.");
        }
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.deleteComment(postId, commentId, authorId);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error deleting comment: " + e.getMessage());
        }
    }

}
