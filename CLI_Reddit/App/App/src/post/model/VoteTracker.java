package post.model;

public class VoteTracker {
    private int upvotes;
    private int downvotes;

    public int getUpvotes(){
        return upvotes;
    }
    public int getDownvotes(){
        return downvotes;
    }
    public void addUpvotes(){
        this.upvotes++;
    }
    public void addDownvotes(){
        this.downvotes++;
    }
    public void removeUpvotes(){
        this.upvotes--;
    }
    public void removeDownvotes(){
        this.downvotes--;
    }

}
