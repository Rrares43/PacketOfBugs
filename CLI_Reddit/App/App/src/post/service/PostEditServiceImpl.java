package post.service;

import post.model.Post;
import post.repository.PostRepository;

public class PostEditServiceImpl implements PostEditService{
    private final PostRepository postRepository;

    public PostEditServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public void editPost(int postId, String newTitle, String newContent) {
        Post postToEdit = postRepository.findPostById(postId);
        if (postToEdit == null) {
            throw new IllegalArgumentException("Post not found");
        }

        if (!postToEdit.getAuthor().equals(postRepository.getCurrentUser())) {
            throw new SecurityException("Only the post owner can edit it");
        }

        postToEdit.setTitle(newTitle);
        postToEdit.setContent(newContent);
        postRepository.saveToFile();
        System.out.println("Post edited successfully");
    }
}
