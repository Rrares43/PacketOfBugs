package post.service;

import persistence.RedditApiClient;
import post.model.Post;
import post.repository.PostRepo;

public class PostEditServiceImpl implements PostEditService {
    private final PostRepo postRepo;

    public PostEditServiceImpl(PostRepo postRepo) {
        this.postRepo = postRepo;
    }

    public void editPost(int postId, String newTitle, String newContent) {
        Post postToEdit = postRepo.findPostById(postId);
        if (postToEdit == null) {
            throw new IllegalArgumentException("Post not found");
        }

        if (!postToEdit.getAuthor().equals(postRepo.getCurrentUser())) {
            throw new SecurityException("Only the post owner can edit it");
        }

        Long accountId = postRepo.getCurrentAccountId();
        if (accountId == null) {
            throw new IllegalStateException("You must be logged in to edit a post.");
        }

        RedditApiClient.editPost(postId, newTitle, newContent, accountId);
        System.out.println("Post edited successfully");
    }
}
