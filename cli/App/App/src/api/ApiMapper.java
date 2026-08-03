package api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import post.model.Comment;
import post.model.Post;
import subreddit.Subreddit;
import util.SubredditNames;

import java.util.ArrayList;
import java.util.List;

public final class ApiMapper {
    private ApiMapper() {
    }

    public static Post toPost(JsonObject json) {
        int id = json.get("id").getAsInt();
        String title = json.has("title") && !json.get("title").isJsonNull()
                ? json.get("title").getAsString() : "";
        String content = json.has("content") && !json.get("content").isJsonNull()
                ? json.get("content").getAsString() : "";
        String author = json.has("authorUsername") && !json.get("authorUsername").isJsonNull()
                ? json.get("authorUsername").getAsString() : "";
        String subreddit = json.has("subredditName") && !json.get("subredditName").isJsonNull()
                ? json.get("subredditName").getAsString() : "";

        Post post = new Post(id, title, content, author, subreddit);
        post.setVoteCounts(readInt(json, "upvotes"), readInt(json, "downvotes"));
        return post;
    }

    public static Post toPostWithComments(JsonObject postJson, JsonArray commentsJson) {
        Post post = toPost(postJson);
        List<Comment> comments = new ArrayList<>();
        if (commentsJson != null) {
            for (JsonElement element : commentsJson) {
                comments.add(toComment(element.getAsJsonObject(), post.getId()));
            }
        }
        for (Comment comment : comments) {
            post.addComment(comment);
        }
        return post;
    }

    public static Comment toComment(JsonObject json, int postId) {
        int id = json.get("id").getAsInt();
        String text = json.has("content") && !json.get("content").isJsonNull()
                ? json.get("content").getAsString() : "";
        String author = json.has("authorUsername") && !json.get("authorUsername").isJsonNull()
                ? json.get("authorUsername").getAsString() : "";

        Comment comment = new Comment(id, text, author);
        boolean deleted = (json.has("deleted") && json.get("deleted").getAsBoolean())
                || (json.has("deletedAt") && !json.get("deletedAt").isJsonNull())
                || "[deleted]".equals(text);
        comment.setDeleted(deleted);
        comment.setPostId(postId);
        comment.setVoteCounts(readInt(json, "upvotes"), readInt(json, "downvotes"));
        if (json.has("replies") && json.get("replies").isJsonArray()) {
            for (JsonElement reply : json.getAsJsonArray("replies")) {
                comment.addReply(toComment(reply.getAsJsonObject(), postId));
            }
        }
        return comment;
    }

    public static Subreddit toSubreddit(JsonObject json) {
        String name = json.has("name") ? json.get("name").getAsString() : "";
        String description = json.has("description") && !json.get("description").isJsonNull()
                ? json.get("description").getAsString() : "";
        String owner = "";
        if (json.has("creatorUsername") && !json.get("creatorUsername").isJsonNull()) {
            owner = json.get("creatorUsername").getAsString();
        } else if (json.has("creatorId") && !json.get("creatorId").isJsonNull()) {
            owner = "id:" + json.get("creatorId").getAsLong();
        }
        Subreddit subreddit = new Subreddit(SubredditNames.normalize(name), description, owner);
        subreddit.setPostCount(json.has("postCount") ? json.get("postCount").getAsInt() : 0);
        return subreddit;
    }

    public static List<Subreddit> toSubredditList(JsonArray array) {
        List<Subreddit> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (JsonElement element : array) {
            result.add(toSubreddit(element.getAsJsonObject()));
        }
        return result;
    }

    public static List<Post> toPostList(JsonArray array) {
        List<Post> result = new ArrayList<>();
        if (array == null) {
            return result;
        }
        for (JsonElement element : array) {
            result.add(toPost(element.getAsJsonObject()));
        }
        return result;
    }

    private static int readInt(JsonObject json, String property) {
        return json.has(property) && !json.get(property).isJsonNull()
                ? json.get(property).getAsInt() : 0;
    }
}
