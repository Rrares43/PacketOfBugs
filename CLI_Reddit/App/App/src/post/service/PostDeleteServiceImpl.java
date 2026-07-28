package post.service;

import post.model.Post;
import post.repository.PostRepository;

public class PostDeleteServiceImpl implements PostDeleteService {
    private final PostRepository postRepository;

    public PostDeleteServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void deletePost(int postId) {
        Post post = postRepository.findPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }

        if (!post.getAuthor().equals(postRepository.getCurrentUser())) {
            throw new SecurityException("Only the post owner can delete it");
        }

        if (!postRepository.removePost(postId)) {
            throw new IllegalStateException("Post could not be deleted");
        }

        postRepository.saveToFile();
    }
}
