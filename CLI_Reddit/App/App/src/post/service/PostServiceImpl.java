package post.service;

import post.model.Post;
import post.repository.PostRepository;

public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private int currentId = 1;

    public PostServiceImpl(PostRepository postRepository) {
        this.postRepository = postRepository;for (Post post : postRepository.findAllPosts()) {
            if (post.getId() >= currentId) {
                currentId = post.getId() + 1;
            }
        }
    }

    @Override
    public Post createPost(String author, String title, String content, String subreddit) {
        int newId = currentId++;
        Post newPost = new Post(newId, title, content, author, subreddit);

        postRepository.addPost(newPost);

        return newPost;
    }
}