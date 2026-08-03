package post.repository;

import post.model.Post;

import java.util.List;

public interface PostRepository {
        Post findPostById(int postId);
        List<Post> findAllPosts();
        List<Post> findPostsBySubreddit(String subredditName);
        String getCurrentUser();
        void addPost(Post post);
        boolean removePost(int postId);
    }
