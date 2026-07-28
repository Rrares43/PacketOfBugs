package persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataMigrator {

    private static final String ACCOUNTS_FILE_PATH = "App/data/accounts.json";
    private static final String SUBREDDITS_FILE_PATH = "App/data/subreddits.json";
    private static final String POSTS_FILE_PATH = "App/data/reddit_database.json";

    private static void cleanDatabase(Connection databaseConnection) throws Exception {
        System.out.println("Cleaning existing database tables...");
        try (Statement stmt = databaseConnection.createStatement()) {
            stmt.execute("TRUNCATE TABLE accounts, subreddits, posts, comments, post_votes, comment_votes CASCADE;");
        }
    }

    public static void runMigration() {

        System.out.println("Starting data migration...");
        try (Connection databaseConnection = DataBaseConnection.getConnection()) {
            cleanDatabase(databaseConnection);

            migrateAccounts(databaseConnection);
            migrateSubreddits(databaseConnection);
            migratePostsAndComments(databaseConnection);

            generateArtificialVotes(databaseConnection);

            System.out.println("Migration completed successfully!");
        } catch (Exception exception) {
            System.err.println("Critical error during migration:");
            exception.printStackTrace();
        }
    }

    private static void migrateAccounts(Connection databaseConnection) throws Exception {
        String insertAccountQuery = "INSERT INTO accounts (username, password) VALUES (?, ?) ON CONFLICT (username) DO NOTHING";

        try (FileReader fileReader = new FileReader(ACCOUNTS_FILE_PATH);
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertAccountQuery)) {

            JsonArray accountsArray = JsonParser.parseReader(fileReader).getAsJsonArray();
            int processedAccountsCount = 0;

            for (JsonElement jsonElement : accountsArray) {
                JsonObject accountObject = jsonElement.getAsJsonObject();

                preparedStatement.setString(1, accountObject.get("username").getAsString());
                preparedStatement.setString(2, accountObject.get("password").getAsString());
                preparedStatement.addBatch();

                processedAccountsCount++;
            }

            preparedStatement.executeBatch();
            System.out.println("-> Processed " + processedAccountsCount + " accounts.");
        }
    }

    private static void migrateSubreddits(Connection databaseConnection) throws Exception {
        String insertSubredditQuery = "INSERT INTO subreddits (name, description, creator_id) " +
                "VALUES (?, ?, (SELECT id FROM accounts WHERE username = ?)) " +
                "ON CONFLICT (name) DO UPDATE " +
                "SET description = EXCLUDED.description, " +
                "creator_id = EXCLUDED.creator_id";

        try (FileReader fileReader = new FileReader(SUBREDDITS_FILE_PATH);
             PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertSubredditQuery)) {

            JsonArray subredditsArray = JsonParser.parseReader(fileReader).getAsJsonArray();
            int processedSubredditsCount = 0;

            for (JsonElement jsonElement : subredditsArray) {
                JsonObject subredditObject = jsonElement.getAsJsonObject();

                preparedStatement.setString(1, subredditObject.get("name").getAsString());
                preparedStatement.setString(2, subredditObject.get("description").getAsString());
                preparedStatement.setString(3, subredditObject.get("owner").getAsString());
                preparedStatement.addBatch();

                processedSubredditsCount++;
            }

            preparedStatement.executeBatch();
            System.out.println("-> Processed " + processedSubredditsCount + " subreddits.");
        }
    }

    private static void migratePostsAndComments(Connection databaseConnection) throws Exception {
        String insertPostQuery = "INSERT INTO posts (title, content, author_id, subreddit_id) " +
                "VALUES (?, ?, (SELECT id FROM accounts WHERE username = ?), (SELECT id FROM subreddits WHERE name = ?)) RETURNING id";

        try (FileReader fileReader = new FileReader(POSTS_FILE_PATH);
             PreparedStatement postPreparedStatement = databaseConnection.prepareStatement(insertPostQuery)) {

            JsonArray postsArray = JsonParser.parseReader(fileReader).getAsJsonArray();
            int processedPostsCount = 0;

            for (JsonElement jsonElement : postsArray) {
                JsonObject postObject = jsonElement.getAsJsonObject();

                postPreparedStatement.setString(1, postObject.get("title").getAsString());
                postPreparedStatement.setString(2, postObject.get("content").getAsString());
                postPreparedStatement.setString(3, postObject.get("author").getAsString());
                postPreparedStatement.setString(4, postObject.get("subredditName").getAsString());

                ResultSet postResultSet = postPreparedStatement.executeQuery();

                if (postResultSet.next()) {
                    int generatedPostId = postResultSet.getInt(1);
                    processedPostsCount++;

                    // If the post has comments, process them
                    if (postObject.has("comments")) {
                        JsonArray commentsArray = postObject.getAsJsonArray("comments");
                        processComments(databaseConnection, commentsArray, generatedPostId, null);
                    }
                }
                postResultSet.close();
            }
            System.out.println("-> Processed " + processedPostsCount + " posts (and their associated comments/replies).");
        }
    }

    // Recursive method for processing comments and their replies
    private static void processComments(Connection databaseConnection, JsonArray commentsArray, int postId, Integer parentCommentId) throws Exception {
        String insertCommentQuery = "INSERT INTO comments (content, post_id, author_id, parent_comment_id) " +
                "VALUES (?, ?, (SELECT id FROM accounts WHERE username = ?), ?) RETURNING id";

        try (PreparedStatement commentPreparedStatement = databaseConnection.prepareStatement(insertCommentQuery)) {

            for (JsonElement jsonElement : commentsArray) {
                JsonObject commentObject = jsonElement.getAsJsonObject();

                commentPreparedStatement.setString(1, commentObject.get("text").getAsString());
                commentPreparedStatement.setInt(2, postId);
                commentPreparedStatement.setString(3, commentObject.get("author").getAsString());

                if (parentCommentId != null) {
                    commentPreparedStatement.setInt(4, parentCommentId);
                } else {
                    commentPreparedStatement.setNull(4, Types.INTEGER);
                }

                ResultSet commentResultSet = commentPreparedStatement.executeQuery();

                if (commentResultSet.next()) {
                    int generatedCommentId = commentResultSet.getInt(1);

                    if (commentObject.has("replies") && commentObject.getAsJsonArray("replies").size() > 0) {
                        JsonArray repliesArray = commentObject.getAsJsonArray("replies");
                        processComments(databaseConnection, repliesArray, postId, generatedCommentId);
                    }
                }
                commentResultSet.close();
            }
        }
    }

    private static void generateArtificialVotes(Connection databaseConnection) throws Exception {
        List<Integer> accountIdsList = new ArrayList<>();
        List<Integer> postIdsList = new ArrayList<>();
        List<Integer> commentIdsList = new ArrayList<>();

        try (Statement statement = databaseConnection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id FROM accounts")) {
            while (resultSet.next()) {
                accountIdsList.add(resultSet.getInt(1));
            }
        }

        try (Statement statement = databaseConnection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id FROM posts")) {
            while (resultSet.next()) {
                postIdsList.add(resultSet.getInt(1));
            }
        }

        try (Statement statement = databaseConnection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT id FROM comments")) {
            while (resultSet.next()) {
                commentIdsList.add(resultSet.getInt(1));
            }
        }

        if (accountIdsList.isEmpty()) {
            System.out.println("No accounts found. Cannot generate votes.");
            return;
        }

        Random randomGenerator = new Random();

        String insertPostVoteQuery = "INSERT INTO post_votes (account_id, post_id, vote_type) VALUES (?, ?, ?) ON CONFLICT (account_id, post_id) DO NOTHING";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertPostVoteQuery)) {
            int postVotesCount = 0;
            for (Integer postId : postIdsList) {
                // Pick a random user to vote
                int randomAccountId = accountIdsList.get(randomGenerator.nextInt(accountIdsList.size()));
                // 80% chance of upvote (1), 20% chance of downvote (-1)
                int voteType = randomGenerator.nextDouble() > 0.2 ? 1 : -1;

                preparedStatement.setInt(1, randomAccountId);
                preparedStatement.setInt(2, postId);
                preparedStatement.setInt(3, voteType);
                preparedStatement.addBatch();
                postVotesCount++;
            }
            preparedStatement.executeBatch();
            System.out.println("-> Generated " + postVotesCount + " artificial post votes.");
        }

        // 5. Generate Comment Votes
        String insertCommentVoteQuery = "INSERT INTO comment_votes (account_id, comment_id, vote_type) VALUES (?, ?, ?) ON CONFLICT (account_id, comment_id) DO NOTHING";
        try (PreparedStatement preparedStatement = databaseConnection.prepareStatement(insertCommentVoteQuery)) {
            int commentVotesCount = 0;
            for (Integer commentId : commentIdsList) {
                int randomAccountId = accountIdsList.get(randomGenerator.nextInt(accountIdsList.size()));
                int voteType = randomGenerator.nextDouble() > 0.2 ? 1 : -1;

                preparedStatement.setInt(1, randomAccountId);
                preparedStatement.setInt(2, commentId);
                preparedStatement.setInt(3, voteType);
                preparedStatement.addBatch();
                commentVotesCount++;
            }
            preparedStatement.executeBatch();
            System.out.println("-> Generated " + commentVotesCount + " artificial comment votes.");
        }
    }
}