package post.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Post {
    private int Id;
    private String title;
    private String content;
    private String author;
    private int upvotes;
    private int downvotes;
    private List<Comment> comments;
    private String subredditName;
    private List<PostVote> votes;
    private VoteTracker voteTracker;

    public Post(int Id,String title,String content,String author, String subredditName){
        this.Id = Id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.subredditName = subredditName;
        this.upvotes = 0;
        this.downvotes = 0;
        this.comments = new ArrayList<>();
        this.votes = new ArrayList<>();
        this.voteTracker = new VoteTracker();
    }

    public int getId(){
        return Id;
    }

    public String getTitle(){
        return title;
    }

    public String getContent(){
        return content;
    }

    public String getAuthor(){
        return author;
    }

    public List<PostVote> getVotes() {
        if (this.votes == null) {
            this.votes = new ArrayList<>();
        }
        return this.votes;
    }

    public int getUpvotes() {
        int count = 0;
        for (PostVote vote : getVotes()) {
            if (vote.isUpvote()) {
                count++;
            }
        }
        return count;
    }

    public int getDownvotes() {
        int count = 0;
        for (PostVote vote : getVotes()) {
            if (!vote.isUpvote()) {
                count++;
            }
        }
        return count;
    }

    public Optional<PostVote> getUserVote(String username) {
        for (PostVote vote : getVotes()) {
            if (vote.getUsername().equals(username)) {
                return Optional.of(vote);
            }
        }
        return Optional.empty();
    }



    public List<Comment> getComments(){
        return comments;
    }

    public String getSubredditName(){
        return subredditName;
    }

    public void addComment(Comment comment){
        this.comments.add(comment);
    }

    public void removeComment(int index){
        if(index>=0 && index<this.comments.size()){
            this.comments.remove(index);
        }
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
