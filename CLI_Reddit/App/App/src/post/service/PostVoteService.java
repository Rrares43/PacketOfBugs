package post.service;

public interface PostVoteService {
    String upvote(int postId,int choice);
    String downvote(int postId,int choice);
}

