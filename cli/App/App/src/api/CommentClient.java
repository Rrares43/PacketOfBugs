package api;

import account.SessionService;

public class CommentClient {
    private final SessionService sessionService;

    public CommentClient(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void comment(int postId, String text) {
        requireLoggedIn("comment");
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.addComment(postId, text, authorId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error commenting: " + e.getMessage());
        }
    }

    public void replyToComment(int postId, int parentCommentId, String text) {
        requireLoggedIn("reply");
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.replyToComment(postId, parentCommentId, text, authorId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error replying: " + e.getMessage());
        }
    }

    public void editComment(int postId, int commentId, String newText) {
        requireLoggedIn("edit a comment");
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.editComment(postId, commentId, newText, authorId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error editing comment: " + e.getMessage());
        }
    }

    public void deleteComment(int postId, int commentId) {
        requireLoggedIn("delete a comment");
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.deleteComment(postId, commentId, authorId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error deleting comment: " + e.getMessage());
        }
    }

    public String upvoteComment(int postId, int commentId, int choice) {
        return handleVote(postId, commentId, true, choice);
    }

    public String downvoteComment(int postId, int commentId, int choice) {
        return handleVote(postId, commentId, false, choice);
    }

    private String handleVote(int postId, int commentId, boolean isUpvote, int choice) {
        if (!sessionService.isLoggedIn()) {
            return "You must be logged in to vote.";
        }
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            return RedditApiClient.voteComment(postId, commentId, authorId, isUpvote, choice);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void requireLoggedIn(String action) {
        if (!sessionService.isLoggedIn()) {
            throw new SecurityException("You must be logged in to " + action + ".");
        }
    }
}
