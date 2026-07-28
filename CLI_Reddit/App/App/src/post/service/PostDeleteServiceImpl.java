package post.service;

import persistence.RedditApiClient;
import post.model.Post;
import post.repository.PostRepo;

public class PostDeleteServiceImpl implements PostDeleteService {
    private final PostRepo postRepo;

    public PostDeleteServiceImpl(PostRepo postRepo) {
        this.postRepo = postRepo;
    }

    @Override
    public void deletePost(int postId) {
        Post post = postRepo.findPostById(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }

        if (!post.getAuthor().equals(postRepo.getCurrentUser())) {
            throw new SecurityException("Only the post owner can delete it");
        }

        Long accountId = postRepo.getCurrentAccountId();
        if (accountId == null) {
            throw new IllegalStateException("You must be logged in to delete a post.");
        }

        RedditApiClient.deletePost(postId, accountId);
    }
}
