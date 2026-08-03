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
    private boolean serverVoteCounts;

    public Post(int Id,String title,String content,String author, String subredditName){
        this.Id = Id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.subredditName = subredditName;
        this.upvotes = 0;
        this.downvotes = 0;
        this.comments = new ArrayList<>();
    }

    public int getId(){
        return Id;
    }

    public void setId(int id) {
        this.Id = id;
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
        this.serverVoteCounts = true;
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

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
