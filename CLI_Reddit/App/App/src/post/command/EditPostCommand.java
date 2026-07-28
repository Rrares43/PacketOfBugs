package post.command;

import post.model.Post;
import post.service.PostEditServiceImpl;
import post.repository.PostRepository;
import io.StringReader;

public class EditPostCommand implements PostActionCommand{
    private final StringReader stringReader;
    private final PostEditServiceImpl postEditService;
    private final PostRepository postRepository;

    public EditPostCommand(StringReader stringReader, PostEditServiceImpl postEditService,
                           PostRepository postRepository) {
        this.stringReader = stringReader;
        this.postEditService = postEditService;
        this.postRepository = postRepository;
    }

    @Override
    public void execute(int postId) {
        Post post = postRepository.findPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }

        if (!post.getAuthor().equals(postRepository.getCurrentUser())) {
            throw new SecurityException("Only the post owner can edit it");
        }

        String newTitle = stringReader.readString("Enter new title: ");
        String newContent = stringReader.readString("Enter new content: ");
        postEditService.editPost(postId, newTitle, newContent);
    }
}
