package post.repository;

import post.model.Post;

import java.util.List;

public interface PostRepository {
        Post findPostById(int postId);
        List<Post> findAllPosts();
        List<Post> findPostsBySubreddit(String subredditName);
        int getNextCommentId();
        String getCurrentUser();
        void saveToFile();
        void addPost(Post post);
        boolean removePost(int postId);
    }
