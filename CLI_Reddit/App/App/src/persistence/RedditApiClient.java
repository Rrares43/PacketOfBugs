package persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import logger.LogLevel;
import logger.Logger;
import util.SubredditNames;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

public final class RedditApiClient {

    private static final Dotenv DOTENV = loadDotenv();
    private static final String BASE_URL = resolveBaseUrl();
    private static final Gson GSON = new GsonBuilder().create();
    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private RedditApiClient() {
    }

    private static Dotenv loadDotenv() {
        try {
            return Dotenv.configure().ignoreIfMissing().load();
        } catch (Exception e) {
            return null;
        }
    }

    private static String resolveBaseUrl() {
        String fromEnv = DOTENV != null ? DOTENV.get("API_BASE_URL") : null;
        if (fromEnv == null || fromEnv.isBlank()) {
            return "http://localhost:8080";
        }
        return fromEnv.endsWith("/") ? fromEnv.substring(0, fromEnv.length() - 1) : fromEnv;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static boolean isReachable() {
        try {
            HttpResponse<String> response = send("GET", "/api/subreddits", null);
            return response.statusCode() >= 200 && response.statusCode() < 500;
        } catch (Exception e) {
            return false;
        }
    }

    public static Optional<JsonObject> getAccount(String username) {
        HttpResponse<String> response = getAccountRaw(username);
        if (response.statusCode() == 200) {
            return Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());
        }
        return Optional.empty();
    }

    public static HttpResponse<String> getAccountRaw(String username) {
        return send("GET", "/api/accounts/" + encode(username), null);
    }

    public static HttpResponse<String> login(String username, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("password", password);
        return send("POST", "/api/accounts/login", body.toString());
    }

    public static HttpResponse<String> registerAccountRaw(String username, String email, String password) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("email", email);
        body.addProperty("password", password);
        return send("POST", "/api/accounts/register", body.toString());
    }

    public static JsonObject registerAccount(String username, String email, String password) {
        HttpResponse<String> response = registerAccountRaw(username, email, password);
        if (response.statusCode() == 400 && response.body() != null
                && response.body().contains("already exists")) {
            return getAccount(username).orElse(null);
        }
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return JsonParser.parseString(response.body()).getAsJsonObject();
        }
        throw new IllegalStateException("Register failed: " + response.statusCode() + " " + response.body());
    }

    public static HttpResponse<String> changePasswordRaw(String username, String email, String newPassword) {
        JsonObject body = new JsonObject();
        body.addProperty("username", username);
        body.addProperty("email", email);
        body.addProperty("newPassword", newPassword);
        return send("PUT", "/api/accounts/password", body.toString());
    }

    public static void changePassword(String username, String oldPassword, String newPassword) {
        HttpResponse<String> response = changePasswordRaw(username, oldPassword, newPassword);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        throw new IllegalStateException("Change password failed: " + response.statusCode() + " " + response.body());
    }

    public static HttpResponse<String> deleteAccountRaw(String username) {
        return send("DELETE", "/api/accounts/" + encode(username), null);
    }

    public static void deleteAccount(String username) {
        HttpResponse<String> response = deleteAccountRaw(username);
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        if (response.statusCode() == 400 || response.statusCode() == 404) {
            return;
        }
        throw new IllegalStateException("Delete account failed: " + response.statusCode() + " " + response.body());
    }

    public static boolean isSuccess(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    public static boolean isClientError(int statusCode) {
        return statusCode >= 400 && statusCode < 500;
    }

    public static long resolveAccountId(String username) {
        return getAccount(username)
                .map(obj -> obj.get("id").getAsLong())
                .orElseThrow(() -> new IllegalStateException("Account not found remotely: " + username));
    }

    public static JsonArray getAllSubreddits() {
        HttpResponse<String> response = send("GET", "/api/subreddits", null);
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    public static Optional<JsonObject> getSubredditByName(String name) {
        HttpResponse<String> response = getSubredditRaw(name);
        if (response.statusCode() == 200) {
            return Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());
        }
        return Optional.empty();
    }

    public static HttpResponse<String> getSubredditRaw(String name) {
        String pathName = SubredditNames.stripPrefix(name);
        return send("GET", "/api/subreddits/" + encode(pathName), null);
    }

    public static JsonArray getSubredditsByCreator(String username) {
        HttpResponse<String> response = send("GET", "/api/subreddits/by-creator/" + encode(username), null);
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    public static JsonObject createSubreddit(String name, String description, long creatorId) {
        JsonObject body = new JsonObject();
        body.addProperty("subredditName", SubredditNames.normalize(name));
        body.addProperty("description", description);
        body.addProperty("creatorId", creatorId);
        HttpResponse<String> response = send("POST", "/api/subreddits", body.toString());
        requireSuccess(response, 201, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject editSubreddit(long id, String name, String description, Long accountId) {
        JsonObject body = new JsonObject();
        body.addProperty("subredditName", name);
        body.addProperty("description", description);
        body.addProperty("accountId", accountId);
        HttpResponse<String> response = send("PUT", "/api/subreddits/" + id, body.toString());
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static void deleteSubreddit(long id, long accountId) {
        JsonObject body = new JsonObject();
        body.addProperty("accountId", accountId);
        HttpResponse<String> response = send("DELETE", "/api/subreddits/" + id, body.toString());
        requireSuccess(response, 200);
    }

    public static JsonArray getAllPosts() {
        HttpResponse<String> response = send("GET", "/api/posts", null);
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    public static Optional<JsonObject> getPost(long id) {
        HttpResponse<String> response = send("GET", "/api/posts/" + id, null);
        if (response.statusCode() == 200) {
            return Optional.of(JsonParser.parseString(response.body()).getAsJsonObject());
        }
        return Optional.empty();
    }

    public static JsonArray getPostsBySubreddit(String subredditName) {
        String pathName = SubredditNames.stripPrefix(subredditName);
        HttpResponse<String> response = send("GET", "/api/posts/subreddit/" + encode(pathName), null);
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    public static JsonObject createPost(String title, String content, long authorId, String subredditName) {
        JsonObject body = new JsonObject();
        body.addProperty("title", title);
        body.addProperty("content", content);
        body.addProperty("authorId", authorId);
        body.addProperty("subredditName", SubredditNames.normalize(subredditName));
        HttpResponse<String> response = send("POST", "/api/posts", body.toString());
        requireSuccess(response, 201, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject editPost(long id, String title, String content, long accountId) {
        JsonObject body = new JsonObject();
        body.addProperty("title", title);
        body.addProperty("content", content);
        body.addProperty("accountId", accountId);
        HttpResponse<String> response = send("PUT", "/api/posts/" + id, body.toString());
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static void deletePost(long id, long accountId) {
        JsonObject body = new JsonObject();
        body.addProperty("accountId", accountId);
        HttpResponse<String> response = send("DELETE", "/api/posts/" + id, body.toString());
        requireSuccess(response, 200);
    }

    public static String votePost(long postId, long accountId, boolean upvote, int choice) {
        JsonObject body = new JsonObject();
        body.addProperty("accountId", accountId);
        body.addProperty("upvote", upvote);
        body.addProperty("choice", choice);
        HttpResponse<String> response = send("POST", "/api/posts/" + postId + "/votes", body.toString());
        requireSuccess(response, 200);
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.has("message") ? json.get("message").getAsString() : response.body();
    }

    public static JsonArray getComments(long postId) {
        HttpResponse<String> response = send("GET", "/api/posts/" + postId + "/comments", null);
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonArray();
    }

    public static JsonObject addComment(long postId, String content, long authorId) {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        body.addProperty("authorId", authorId);
        HttpResponse<String> response = send("POST", "/api/posts/" + postId + "/comments", body.toString());
        requireSuccess(response, 201, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject replyToComment(long postId, long parentCommentId, String content, long authorId) {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        body.addProperty("authorId", authorId);
        HttpResponse<String> response = send("POST",
                "/api/posts/" + postId + "/comments/" + parentCommentId + "/replies",
                body.toString());
        requireSuccess(response, 201, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static JsonObject editComment(long postId, long commentId, String content, long accountId) {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        body.addProperty("accountId", accountId);
        HttpResponse<String> response = send("PUT",
                "/api/posts/" + postId + "/comments/" + commentId,
                body.toString());
        requireSuccess(response, 200);
        return JsonParser.parseString(response.body()).getAsJsonObject();
    }

    public static void deleteComment(long postId, long commentId, long accountId) {
        JsonObject body = new JsonObject();
        body.addProperty("accountId", accountId);
        HttpResponse<String> response = send("DELETE",
                "/api/posts/" + postId + "/comments/" + commentId,
                body.toString());
        requireSuccess(response, 200);
    }

    public static String voteComment(long postId, long commentId, long accountId, boolean upvote, int choice) {
        JsonObject body = new JsonObject();
        body.addProperty("accountId", accountId);
        body.addProperty("upvote", upvote);
        body.addProperty("choice", choice);
        HttpResponse<String> response = send("POST",
                "/api/posts/" + postId + "/comments/" + commentId + "/votes",
                body.toString());
        requireSuccess(response, 200);
        JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
        return json.has("message") ? json.get("message").getAsString() : response.body();
    }

    public static Optional<Long> findPostIdForComment(long commentId) {
        JsonArray posts = getAllPosts();
        for (JsonElement element : posts) {
            long postId = element.getAsJsonObject().get("id").getAsLong();
            if (commentExistsInTree(getComments(postId), commentId)) {
                return Optional.of(postId);
            }
        }
        return Optional.empty();
    }

    private static boolean commentExistsInTree(JsonArray comments, long commentId) {
        for (JsonElement element : comments) {
            JsonObject comment = element.getAsJsonObject();
            if (comment.get("id").getAsLong() == commentId) {
                return true;
            }
            if (comment.has("replies") && commentExistsInTree(comment.getAsJsonArray("replies"), commentId)) {
                return true;
            }
        }
        return false;
    }

    private static HttpResponse<String> send(String method, String path, String jsonBody) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json");

            if (jsonBody != null) {
                builder.header("Content-Type", "application/json");
                builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else if ("DELETE".equals(method)) {
                builder.DELETE();
            } else {
                builder.GET();
            }

            return CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            throw new IllegalStateException("HTTP " + method + " " + path + " failed: " + e.getMessage(), e);
        }
    }

    public static JsonArray getLogs() {
        HttpResponse<String> response = send("GET", "/api/logs", null);
        requireSuccess(response, 200);
        JsonElement parsed = JsonParser.parseString(response.body());
        if (parsed.isJsonArray()) {
            return parsed.getAsJsonArray();
        }
        JsonArray wrapper = new JsonArray();
        wrapper.add(parsed);
        return wrapper;
    }

    private static void requireSuccess(HttpResponse<String> response, int... okCodes) {
        for (int code : okCodes) {
            if (response.statusCode() == code) {
                return;
            }
        }
        String body = response.body() != null ? response.body() : "";
        if (isClientError(response.statusCode())) {
            throw new IllegalStateException(body.isBlank()
                    ? "Request failed with status " + response.statusCode()
                    : body);
        }
        throw new IllegalStateException(
                "Unexpected status " + response.statusCode() + ": " + body);
    }

    private static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8)
                    .replace("+", "%20");
        } catch (Exception e) {
            return value.replace(" ", "%20");
        }
    }
    public static Gson gson() {
        return GSON;
    }

    public static void logFailure(String entity, Exception e) {
        String message = "API dual-write failed for " + entity + ": " + e.getMessage();
        System.err.println(message);
        Logger.getInstance().log(LogLevel.ERROR, message);
    }
}
