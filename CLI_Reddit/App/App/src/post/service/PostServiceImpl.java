package post.service;

import account.SessionService;
import com.google.gson.JsonObject;
import persistence.ApiMapper;
import persistence.RedditApiClient;
import post.model.Post;
import util.SubredditNames;

public class PostServiceImpl implements PostService {
    private final SessionService sessionService;

    public PostServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Post createPost(String author, String title, String content, String subreddit) {
        Long authorId = sessionService.getCurrentAccountId();
        if (authorId == null) {
            authorId = RedditApiClient.resolveAccountId(author);
        }

        JsonObject created = RedditApiClient.createPost(
                title,
                content,
                authorId,
                SubredditNames.normalize(subreddit));
        return ApiMapper.toPost(created);
    }
}
