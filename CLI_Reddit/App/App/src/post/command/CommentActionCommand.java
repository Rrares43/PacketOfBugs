package post.command;

public interface CommentActionCommand {
    void execute(int postId, int commentId);
}