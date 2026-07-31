package post.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Comment {
    private int Id;
    private String text;
    private String author;
    private List<Comment> replies;
    private List<CommentVote> votes;
    private VoteTracker voteTracker;
    private int postId;
    private Integer parentId;
    private int upvotes;
    private int downvotes;
    private boolean serverVoteCounts;
    private boolean deleted;

    public Comment(int Id, String text, String author) {
        this.Id = Id;
        this.text = text;
        this.author = author;
        this.replies = new ArrayList<>();
        this.votes = new ArrayList<>();
        this.voteTracker = new VoteTracker();
    }

    public void setText(String newText) {
        this.text = newText;
    }

    public void setId(int id) {
        this.Id = id;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getId() {
        return Id;
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

    public void addreply(Comment reply) {
        getReplies().add(reply);
    }

    public int getPostId() {
        return this.postId;
    }
    public List<CommentVote> getVotes() {
        if (votes == null) {
            votes = new ArrayList<>();
        }
        return votes;
    }

    public int getUpvotes() {
        if (serverVoteCounts) return upvotes;
        int count = 0;
        for (CommentVote vote : getVotes()) if (vote.isUpvote()) count++;
        return count;
    }

    public int getDownvotes() {
        if (serverVoteCounts) return downvotes;
        int count = 0;
        for (CommentVote vote : getVotes()) if (!vote.isUpvote()) count++;
        return count;
    }

    public void setVoteCounts(int upvotes, int downvotes) {
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.serverVoteCounts = true;
    }

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public Optional<CommentVote> getUserVote(String username) {
        for (CommentVote vote : getVotes()) {
            if (vote.getUsername().equals(username)) {
                return Optional.of(vote);
            }
        }
        return Optional.empty();
    }
    @Override
    public String toString() {
        if (deleted) {
            return "[ID: " + Id + "] [deleted]: [deleted]";
        }
        return "[ID: " + Id + "] " + author + ": " + text +
                " (▲ " + getUpvotes() + " | ▼ " + getDownvotes() + ")";
    }
}

