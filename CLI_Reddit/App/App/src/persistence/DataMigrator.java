package persistence;

import account.Account;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import post.model.Comment;
import post.model.Post;
import subreddit.Subreddit;

import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * One-time JSON → Spring API migration helper (no direct JDBC).
 * Kept for compatibility with the commented call in {@code Main}.
 */
public class DataMigrator {

    private static final Gson GSON = new GsonBuilder().create();

    public static void runMigration() {
        System.out.println("Starting data migration via Spring API...");
        try {
            List<Account> accounts = loadAccounts();
            DatabaseSync.syncAccounts(accounts);
            System.out.println("-> Accounts synced.");

            List<Subreddit> subreddits = loadSubreddits();
            DatabaseSync.syncSubreddits(subreddits);
            System.out.println("-> Subreddits synced.");

            List<Post> posts = loadPosts();
            DatabaseSync.syncPosts(posts);
            System.out.println("-> Posts and comments synced.");

            generateArtificialVotes(accounts, posts);

            System.out.println("Migration completed successfully!");
        } catch (Exception exception) {
            System.err.println("Critical error during migration:");
            exception.printStackTrace();
        }
    }

    private static List<Account> loadAccounts() throws Exception {
        try (FileReader reader = new FileReader(DataPaths.resolveDataFile("accounts.json").toFile())) {
            Type type = new TypeToken<ArrayList<Account>>() {}.getType();
            List<Account> accounts = GSON.fromJson(reader, type);
            return accounts != null ? accounts : new ArrayList<>();
        }
    }

    private static List<Subreddit> loadSubreddits() throws Exception {
        try (FileReader reader = new FileReader(DataPaths.resolveDataFile("subreddits.json").toFile())) {
            Type type = new TypeToken<ArrayList<Subreddit>>() {}.getType();
            List<Subreddit> subreddits = GSON.fromJson(reader, type);
            return subreddits != null ? subreddits : new ArrayList<>();
        }
    }

    private static List<Post> loadPosts() throws Exception {
        try (FileReader reader = new FileReader(DataPaths.resolveDataFile("reddit_database.json").toFile())) {
            Type type = new TypeToken<ArrayList<Post>>() {}.getType();
            List<Post> posts = GSON.fromJson(reader, type);
            return posts != null ? posts : new ArrayList<>();
        }
    }

    private static void generateArtificialVotes(List<Account> accounts, List<Post> posts) {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found. Cannot generate votes.");
            return;
        }

        Random random = new Random();
        int postVotesCount = 0;
        int commentVotesCount = 0;

        for (Post post : posts) {
            Account voter = accounts.get(random.nextInt(accounts.size()));
            int voteType = random.nextDouble() > 0.2 ? 1 : -1;
            DatabaseSync.upsertPostVote(voter.getUsername(), post.getId(), voteType);
            postVotesCount++;

            commentVotesCount += voteComments(accounts, post.getComments(), random);
        }

        System.out.println("-> Generated " + postVotesCount + " artificial post votes.");
        System.out.println("-> Generated " + commentVotesCount + " artificial comment votes.");
    }

    private static int voteComments(List<Account> accounts, List<Comment> comments, Random random) {
        if (comments == null) {
            return 0;
        }
        int count = 0;
        for (Comment comment : comments) {
            Account voter = accounts.get(random.nextInt(accounts.size()));
            int voteType = random.nextDouble() > 0.2 ? 1 : -1;
            DatabaseSync.upsertCommentVote(voter.getUsername(), comment.getId(), voteType);
            count++;
            count += voteComments(accounts, comment.getReplies(), random);
        }
        return count;
    }
}
