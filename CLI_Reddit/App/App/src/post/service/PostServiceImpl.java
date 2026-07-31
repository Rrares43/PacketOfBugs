package post.service;

import account.SessionService;
import com.google.gson.JsonObject;
import persistence.ApiMapper;
import persistence.RedditApiClient;
import post.model.Post;
import post.repository.PostRepository;

public class PostServiceImpl implements PostService {
    private final SessionService sessionService;

    public PostServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Post createPost(String author, String title, String content, String subreddit) {
        long authorId = sessionService.getCurrentAccountId();

        JsonObject post = RedditApiClient.createPost(title, content, authorId, subreddit);

        return ApiMapper.toPost(post);
    }
}