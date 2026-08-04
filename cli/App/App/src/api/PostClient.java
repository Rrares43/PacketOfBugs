package api;

import account.SessionService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import post.model.Post;

import java.util.List;
import java.util.Optional;

public class PostClient {
    private final SessionService sessionService;

    public PostClient(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public Post findPostById(int postId) {
        Optional<JsonObject> postJson = RedditApiClient.getPost(postId);
        if (postJson.isPresent()) {
            JsonArray comments = RedditApiClient.getComments(postId);
            return ApiMapper.toPostWithComments(postJson.get(), comments);
        }
        return null;
    }

    public String getCurrentUser() {
        return sessionService.getCurrentUsername();
    }

    public List<Post> findPostsBySubreddit(String subredditName) {
        return ApiMapper.toPostList(RedditApiClient.getPostsBySubreddit(subredditName));
    }

    public Post createPost(String author, String title, String content, String subreddit) {
        Long authorId = sessionService.getCurrentAccountId();
        if (authorId == null) {
            authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
        }
        JsonObject post = RedditApiClient.createPost(title, content, authorId, subreddit);
        return ApiMapper.toPost(post);
    }

    public void editPost(int postId, String newTitle, String newContent) {
        requireLoggedIn("edit a post");
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.editPost(postId, newTitle, newContent, authorId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error editing post: " + e.getMessage());
        }
    }

    public void deletePost(int postId) {
        requireLoggedIn("delete a post");
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.deletePost(postId, authorId);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error deleting post: " + e.getMessage());
        }
    }

    public String upvote(int postId, int choice) {
        return handleVote(postId, choice, true);
    }

    public String downvote(int postId, int choice) {
        return handleVote(postId, choice, false);
    }

    private String handleVote(int postId, int choice, boolean isUpvote) {
        if (!sessionService.isLoggedIn()) {
            return "You must be logged in to vote.";
        }
        try {
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            return RedditApiClient.votePost(postId, authorId, isUpvote, choice);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private void requireLoggedIn(String action) {
        if (!sessionService.isLoggedIn()) {
            throw new SecurityException("You must be logged in to " + action + ".");
        }
    }
}
