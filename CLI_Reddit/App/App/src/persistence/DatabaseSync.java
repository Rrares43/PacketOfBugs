package persistence;

import account.Account;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import post.model.Comment;
import post.model.Post;
import subreddit.Subreddit;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Dual-writes local JSON state to the Spring Boot backend via HTTP.
 * Public method signatures are unchanged so repositories/services keep working.
 * Generated remote IDs from create responses are mapped back onto local models.
 */
public final class DatabaseSync {
    private DatabaseSync() {}

    public static void syncAccounts(List<Account> accounts) {
        for (Account account : accounts) {
            try {
                Optional<JsonObject> remote = RedditApiClient.getAccount(account.getUsername());
                if (remote.isEmpty()) {
                    RedditApiClient.registerAccount(
                            account.getUsername(),
                            account.getEmail(),
                            account.getPassword());
                }
            } catch (Exception e) {
                RedditApiClient.logFailure("account:" + account.getUsername(), e);
            }
        }
    }

    public static void syncSubreddits(List<Subreddit> subreddits) {
        Set<String> keepNames = new HashSet<>();
        for (Subreddit subreddit : subreddits) {
            keepNames.add(subreddit.getName());
            try {
                Optional<JsonObject> remote = RedditApiClient.getSubredditByName(subreddit.getName());
                if (remote.isEmpty()) {
                    long creatorId = RedditApiClient.resolveAccountId(subreddit.getOwner());
                    RedditApiClient.createSubreddit(
                            subreddit.getName(),
                            subreddit.getDescription(),
                            creatorId);
                } else {
                    long id = remote.get().get("id").getAsLong();
                    RedditApiClient.editSubreddit(id, subreddit.getName(), subreddit.getDescription());
                }
            } catch (Exception e) {
                RedditApiClient.logFailure("subreddit:" + subreddit.getName(), e);
            }
        }

        try {
            JsonArray remoteSubs = RedditApiClient.getAllSubreddits();
            for (JsonElement element : remoteSubs) {
                JsonObject remote = element.getAsJsonObject();
                String name = remote.get("name").getAsString();
                if (!keepNames.contains(name)) {
                    try {
                        RedditApiClient.deleteSubreddit(remote.get("id").getAsLong());
                    } catch (Exception e) {
                        RedditApiClient.logFailure("subreddit-delete:" + name, e);
                    }
                }
            }
        } catch (Exception e) {
            RedditApiClient.logFailure("subreddits", e);
        }
    }

    public static void syncPosts(List<Post> posts) {
        Set<Long> keepPostIds = new HashSet<>();
        for (Post post : posts) {
            try {
                long authorId = RedditApiClient.resolveAccountId(post.getAuthor());
                Optional<JsonObject> remote = RedditApiClient.getPost(post.getId());
                if (remote.isPresent()) {
                    RedditApiClient.editPost(post.getId(), post.getTitle(), post.getContent(), authorId);
                } else {
                    JsonObject created = RedditApiClient.createPost(
                            post.getTitle(),
                            post.getContent(),
                            authorId,
                            post.getSubredditName());
                    if (created != null && created.has("id") && !created.get("id").isJsonNull()) {
                        int remoteId = created.get("id").getAsInt();
                        if (remoteId != post.getId()) {
                            post.setId(remoteId);
                        }
                    }
                }
                keepPostIds.add((long) post.getId());
                syncCommentsForPost(post.getId(), post.getComments());
            } catch (Exception e) {
                RedditApiClient.logFailure("post:" + post.getId(), e);
            }
        }

        try {
            JsonArray remotePosts = RedditApiClient.getAllPosts();
            for (JsonElement element : remotePosts) {
                JsonObject remote = element.getAsJsonObject();
                long remoteId = remote.get("id").getAsLong();
                if (!keepPostIds.contains(remoteId)) {
                    try {
                        long authorId = remote.has("authorId") && !remote.get("authorId").isJsonNull()
                                ? remote.get("authorId").getAsLong()
                                : RedditApiClient.resolveAccountId(remote.get("authorUsername").getAsString());
                        RedditApiClient.deletePost(remoteId, authorId);
                    } catch (Exception e) {
                        RedditApiClient.logFailure("post-delete:" + remoteId, e);
                    }
                }
            }
        } catch (Exception e) {
            RedditApiClient.logFailure("posts", e);
        }
    }

    public static void upsertPostVote(String username, int postId, int voteType) {
        try {
            long accountId = RedditApiClient.resolveAccountId(username);
            RedditApiClient.votePost(postId, accountId, voteType == 1, 1);
        } catch (Exception e) {
            RedditApiClient.logFailure("post_votes", e);
        }
    }

    public static void removePostVote(String username, int postId, int voteType) {
        try {
            long accountId = RedditApiClient.resolveAccountId(username);
            RedditApiClient.votePost(postId, accountId, voteType == 1, 2);
        } catch (Exception e) {
            RedditApiClient.logFailure("post_votes", e);
        }
    }

    public static void upsertCommentVote(String username, int commentId, int voteType) {
        try {
            long accountId = RedditApiClient.resolveAccountId(username);
            long postId = RedditApiClient.findPostIdForComment(commentId)
                    .orElseThrow(() -> new IllegalStateException("No post found for comment " + commentId));
            RedditApiClient.voteComment(postId, commentId, accountId, voteType == 1, 1);
        } catch (Exception e) {
            RedditApiClient.logFailure("comment_votes", e);
        }
    }

    public static void removeCommentVote(String username, int commentId, int voteType) {
        try {
            long accountId = RedditApiClient.resolveAccountId(username);
            long postId = RedditApiClient.findPostIdForComment(commentId)
                    .orElseThrow(() -> new IllegalStateException("No post found for comment " + commentId));
            RedditApiClient.voteComment(postId, commentId, accountId, voteType == 1, 2);
        } catch (Exception e) {
            RedditApiClient.logFailure("comment_votes", e);
        }
    }

    private static void syncCommentsForPost(int postId, List<Comment> localComments) {
        Set<Long> keepIds = new HashSet<>();
        JsonArray remoteComments = RedditApiClient.getComments(postId);
        upsertCommentTree(postId, localComments, null, flattenRemoteComments(remoteComments), keepIds);
        deleteOrphanComments(postId, remoteComments, keepIds);
    }

    private static void upsertCommentTree(int postId,
                                          List<Comment> comments,
                                          Long parentCommentId,
                                          Set<Long> remoteIds,
                                          Set<Long> keepIds) {
        if (comments == null) {
            return;
        }
        for (Comment comment : comments) {
            comment.setPostId(postId);
            long authorId = RedditApiClient.resolveAccountId(comment.getAuthor());
            long commentId = comment.getId();
            if (remoteIds.contains(commentId)) {
                RedditApiClient.editComment(postId, commentId, comment.getText(), authorId);
            } else {
                JsonObject created;
                if (parentCommentId == null) {
                    created = RedditApiClient.addComment(postId, comment.getText(), authorId);
                } else {
                    created = RedditApiClient.replyToComment(postId, parentCommentId, comment.getText(), authorId);
                }
                if (created != null && created.has("id") && !created.get("id").isJsonNull()) {
                    int remoteId = created.get("id").getAsInt();
                    if (remoteId != comment.getId()) {
                        comment.setId(remoteId);
                        commentId = remoteId;
                    }
                    remoteIds.add(commentId);
                }
            }
            keepIds.add(commentId);
            upsertCommentTree(postId, comment.getReplies(), commentId, remoteIds, keepIds);
        }
    }

    private static void deleteOrphanComments(int postId, JsonArray remoteComments, Set<Long> keepIds) {
        deleteOrphanCommentNodes(postId, remoteComments, keepIds);
    }

    private static void deleteOrphanCommentNodes(int postId, JsonArray comments, Set<Long> keepIds) {
        if (comments == null) {
            return;
        }
        for (JsonElement element : comments) {
            JsonObject comment = element.getAsJsonObject();
            long id = comment.get("id").getAsLong();
            if (comment.has("replies")) {
                deleteOrphanCommentNodes(postId, comment.getAsJsonArray("replies"), keepIds);
            }
            if (!keepIds.contains(id)) {
                long authorId = comment.get("authorId").getAsLong();
                try {
                    RedditApiClient.deleteComment(postId, id, authorId);
                } catch (Exception ignored) {
                    // Parent may already cascade-delete children.
                }
            }
        }
    }

    private static Set<Long> flattenRemoteComments(JsonArray comments) {
        Set<Long> ids = new HashSet<>();
        collectRemoteCommentIds(comments, ids);
        return ids;
    }

    private static void collectRemoteCommentIds(JsonArray comments, Set<Long> ids) {
        if (comments == null) {
            return;
        }
        for (JsonElement element : comments) {
            JsonObject comment = element.getAsJsonObject();
            ids.add(comment.get("id").getAsLong());
            if (comment.has("replies")) {
                collectRemoteCommentIds(comment.getAsJsonArray("replies"), ids);
            }
        }
    }
}
