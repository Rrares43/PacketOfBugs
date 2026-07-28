package post.command;

import post.service.PostDeleteService;
import io.OutputWriter;

public class DeletePostCommand implements PostActionCommand {
    private final PostDeleteService postDeleteService;
    private final OutputWriter output;

    public DeletePostCommand(PostDeleteService postDeleteService, OutputWriter output) {
        this.postDeleteService = postDeleteService;
        this.output = output;
    }

    @Override
    public void execute(int postId) {
        postDeleteService.deletePost(postId);
        output.write("Post deleted successfully.");
    }
}
