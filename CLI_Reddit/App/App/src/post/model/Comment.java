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
    private int upvotes;
    private int downvotes;

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

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public int getId() {
        return Id;
    }

    public String getText() {
        return text;
    }

    public String getAuthor() {
        return author;
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
        if (getVotes().isEmpty()) {
            return upvotes;
        }
        int count = 0;
        for (CommentVote vote : getVotes()) {
            if (vote.isUpvote()) {
                count++;
            }
        }
        return count;
    }

    public int getDownvotes() {
        if (getVotes().isEmpty()) {
            return downvotes;
        }
        int count = 0;
        for (CommentVote vote : getVotes()) {
            if (!vote.isUpvote()) {
                count++;
            }
        }
        return count;
    }

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
        return "[ID: " + Id + "] " + author + ": " + text +
                " (▲ " + getUpvotes() + " | ▼ " + getDownvotes() + ")";
    }
}
