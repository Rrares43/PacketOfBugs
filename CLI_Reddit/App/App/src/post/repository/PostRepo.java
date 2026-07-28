package post.repository;

import account.SessionService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import persistence.ApiMapper;
import persistence.RedditApiClient;
import post.model.Comment;
import post.model.Post;
import util.SubredditNames;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HTTP-backed post repository. All reads/writes go to the Spring Boot API.
 */
public class PostRepo implements PostRepository {
    private final SessionService sessionService;

    public PostRepo(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Post findPostById(int postId) {
        Optional<JsonObject> remote = RedditApiClient.getPost(postId);
        if (remote.isEmpty()) {
            return null;
        }
        JsonArray comments = RedditApiClient.getComments(postId);
        return ApiMapper.toPostWithComments(remote.get(), comments);
    }

    @Override
    public List<Post> findAllPosts() {
        return ApiMapper.toPostList(RedditApiClient.getAllPosts());
    }

    @Override
    public List<Post> findPostsBySubreddit(String subredditName) {
        String normalized = SubredditNames.normalize(subredditName);
        return ApiMapper.toPostList(RedditApiClient.getPostsBySubreddit(normalized));
    }

    @Override
    public int getNextCommentId() {
        return 0;
    }

    @Override
    public String getCurrentUser() {
        return sessionService.getCurrentUsername();
    }

    public Long getCurrentAccountId() {
        Long id = sessionService.getCurrentAccountId();
        if (id != null) {
            return id;
        }
        if (!sessionService.isLoggedIn()) {
            return null;
        }
        try {
            return RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void addPost(Post post) {
        Long authorId = getCurrentAccountId();
        if (authorId == null) {
            throw new IllegalStateException("You must be logged in to create a post.");
        }
        RedditApiClient.createPost(
                post.getTitle(),
                post.getContent(),
                authorId,
                SubredditNames.normalize(post.getSubredditName()));
    }

    @Override
    public boolean removePost(int postId) {
        Long accountId = getCurrentAccountId();
        if (accountId == null) {
            return false;
        }
        try {
            RedditApiClient.deletePost(postId, accountId);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public Comment findCommentById(int postId, int commentId) {
        Post post = findPostById(postId);
        if (post == null) {
            return null;
        }
        return searchInComments(post.getComments(), commentId);
    }

    public boolean removeComment(int postId, int commentId) {
        Long accountId = getCurrentAccountId();
        if (accountId == null) {
            return false;
        }
        try {
            RedditApiClient.deleteComment(postId, commentId, accountId);
            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private Comment searchInComments(List<Comment> comments, int commentId) {
        if (comments == null) {
            return null;
        }
        for (Comment c : comments) {
            if (c.getId() == commentId) {
                return c;
            }
            Comment foundInReplies = searchInComments(c.getReplies(), commentId);
            if (foundInReplies != null) {
                return foundInReplies;
            }
        }
        return null;
    }
}
