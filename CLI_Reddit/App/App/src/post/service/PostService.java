package post.service;

import post.model.Post;

public interface PostService {
    Post createPost(String author, String title, String content, String subreddit);
}
