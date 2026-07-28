package subreddit.repository;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import persistence.ApiMapper;
import persistence.RedditApiClient;
import subreddit.Subreddit;
import util.SubredditNames;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * HTTP-backed subreddit repository. No local file storage.
 */
public class SubredditRepository {

    private SubredditRepository() {
    }

    public static List<Subreddit> loadSubreddits() {
        try {
            return ApiMapper.toSubredditList(RedditApiClient.getAllSubreddits());
        } catch (Exception e) {
            System.out.println("Failed to load subreddits: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public static Optional<Subreddit> findByName(String name) {
        String normalized = SubredditNames.normalize(name);
        try {
            HttpResponse<String> response = RedditApiClient.getSubredditRaw(normalized);
            if (RedditApiClient.isSuccess(response.statusCode())) {
                JsonObject json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
                return Optional.of(ApiMapper.toSubreddit(json));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static void listSubsMadebyUser(String user) {
        try {
            JsonArray array = RedditApiClient.getSubredditsByCreator(user);
            List<Subreddit> subreddits = ApiMapper.toSubredditList(array);
            if (subreddits.isEmpty()) {
                System.out.println("(none)");
                return;
            }
            for (Subreddit sub : subreddits) {
                sub.setOwner(user);
                System.out.println(sub.getName());
            }
        } catch (Exception e) {
            System.out.println("Failed to list subreddits: " + e.getMessage());
        }
    }

    public static void saveSubreddit(Subreddit subreddit) {
        try {
            long creatorId = RedditApiClient.resolveAccountId(subreddit.getOwner());
            RedditApiClient.createSubreddit(
                    SubredditNames.normalize(subreddit.getName()),
                    subreddit.getDescription(),
                    creatorId);
            System.out.println("Subreddit saved successfully!");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void updateSubreddit(long id, String name, String description) {
        RedditApiClient.editSubreddit(id, SubredditNames.normalize(name), description);
    }

    public static void deleteSubreddit(long id) {
        RedditApiClient.deleteSubreddit(id);
    }
}
