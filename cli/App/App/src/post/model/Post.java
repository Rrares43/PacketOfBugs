package post.model;

import java.util.ArrayList;
import java.util.List;

public class Post {
    private final int id;
    private String title;
    private String content;
    private String author;
    private int upvotes;
    private int downvotes;
    private List<Comment> comments;
    private String subredditName;

    public Post(int id, String title, String content, String author, String subredditName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.subredditName = subredditName;
        this.upvotes = 0;
        this.downvotes = 0;
        this.comments = new ArrayList<>();
    }

    public int getId() {
        return id;
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



    public List<Comment> getComments(){
        if (comments == null) {
            comments = new ArrayList<>();
        }
        return comments;
    }

    public String getSubredditName(){
        return subredditName;
    }

    public void addComment(Comment comment){
        getComments().add(comment);
    }

}
