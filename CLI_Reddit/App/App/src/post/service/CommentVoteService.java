package post.service;

public interface CommentVoteService {
    String upvoteComment(int postId,int commentId,int choice);
    String downvoteComment(int postId,int commentId,int choice);
}
