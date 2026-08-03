package post.model;

import java.util.ArrayList;
import java.util.List;

public class Comment {
    private final int id;
    private String text;
    private String author;
    private List<Comment> replies;
    private int postId;
    private int upvotes;
    private int downvotes;
    private boolean deleted;

    public Comment(int id, String text, String author) {
        this.id = id;
        this.text = text;
        this.author = author;
        this.replies = new ArrayList<>();
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return deleted ? "[deleted]" : author;
    }

    public boolean isDeleted() { return deleted; }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
        if (deleted) {
            this.author = "[deleted]";
            this.text = "[deleted]";
        }
    }

    public List<Comment> getReplies() {
        if (replies == null) {
            replies = new ArrayList<>();
        }
        return replies;
    }

    public void addReply(Comment reply) {
        getReplies().add(reply);
    }

    public int getPostId() {
        return this.postId;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setVoteCounts(int upvotes, int downvotes) {
        this.upvotes = upvotes;
        this.downvotes = downvotes;
    }

    @Override
    public String toString() {
        if (deleted) {
            return "[ID: " + id + "] [deleted]: [deleted]";
        }
        return "[ID: " + id + "] " + author + ": " + text +
                " (▲ " + getUpvotes() + " | ▼ " + getDownvotes() + ")";
    }
}

