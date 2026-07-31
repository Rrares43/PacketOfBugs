package post.service;

import post.model.Comment;

public interface CommentService {
    public void comment(int postId, String text);
    public void replyToComment(int postId, int parentCommentId, String text);
    public void editComment(int postId, int commentId, String newText);
    public void deleteComment(int postId, int commentId);
}
