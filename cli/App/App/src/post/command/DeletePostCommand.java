package post.command;

import api.PostClient;
import io.OutputWriter;

public class DeletePostCommand implements PostActionCommand {
    private final PostClient postClient;
    private final OutputWriter output;

    public DeletePostCommand(PostClient postClient, OutputWriter output) {
        this.postClient = postClient;
        this.output = output;
    }

    @Override
    public void execute(int postId) {
        postClient.deletePost(postId);
        output.write("Post deleted successfully.");
    }
}
