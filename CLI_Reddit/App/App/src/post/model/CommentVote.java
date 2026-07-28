package post.model;

public class CommentVote {
    private String usernamme;
    private int commentId;
    private boolean isUpvote;
    public CommentVote(String usernamme, int commentId, boolean isUpvote) {
        this.usernamme = usernamme;
        this.commentId = commentId;
        this.isUpvote = isUpvote;
    }
    public String getUsername() {
        return usernamme;
    }
    public int getCommentId() {
        return commentId;
    }
    public boolean isUpvote() {
        return isUpvote;
    }
    public void setUpvote(boolean upvote) {
        isUpvote = upvote;
    }
}
