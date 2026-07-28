package post.model;

public class PostVote {
    private String username;
    private int postId;
    private boolean isUpvote;

    public PostVote(String username, int postId, boolean isUpvote) {
        this.username = username;
        this.postId = postId;
        this.isUpvote = isUpvote;
    }
    public String getUsername() {
        return username;
    }
    public int getPostId() {
        return postId;
    }
    public boolean isUpvote() {
        return isUpvote;
    }
    public void setUpvote(boolean upvote) {
        isUpvote = upvote;
    }
}
