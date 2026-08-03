package post.command;

import api.PostClient;
import post.model.Post;
import io.StringReader;

public class EditPostCommand implements PostActionCommand {
    private final StringReader stringReader;
    private final PostClient postClient;

    public EditPostCommand(StringReader stringReader, PostClient postClient) {
        this.stringReader = stringReader;
        this.postClient = postClient;
    }

    @Override
    public void execute(int postId) {
        Post post = postClient.findPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }

        if (!post.getAuthor().equals(postClient.getCurrentUser())) {
            throw new SecurityException("Only the post owner can edit it");
        }

        String newTitle = stringReader.readString("Enter new title: ");
        String newContent = stringReader.readString("Enter new content: ");
        postClient.editPost(postId, newTitle, newContent);
    }
}
