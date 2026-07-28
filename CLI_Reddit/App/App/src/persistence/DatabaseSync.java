package persistence;

import account.Account;
import subreddit.Subreddit;
import post.model.Comment;
import post.model.Post;
import logger.LogLevel;
import logger.Logger;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DatabaseSync {
    private DatabaseSync() {}

    public static void syncAccounts(List<Account> accounts) {
        try (Connection connection = DataBaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try{
                String sql = "INSERT INTO accounts (username, password) VALUES (?, ?) "
                        + "ON CONFLICT (username) DO UPDATE SET password = EXCLUDED.password";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Account account : accounts) {
                        statement.setString(1, account.getUsername());
                        statement.setString(2, account.getPassword());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                deleteOrphanAccounts(connection, accounts);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
        catch (Exception e){
            logDbFailure("accounts", e);
        }
    }

    public static void syncSubreddits(List<Subreddit> subreddits) {
        try (Connection connection = DataBaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try{
                String sql = "INSERT INTO subreddits (name, description, creator_id) "
                        + "VALUES (?, ?, (SELECT id FROM accounts WHERE username = ?)) "
                        + "ON CONFLICT (name) DO UPDATE SET description = EXCLUDED.description, "
                        + "creator_id = EXCLUDED.creator_id";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    for (Subreddit subreddit : subreddits) {
                        statement.setString(1, subreddit.getName());
                        statement.setString(2, subreddit.getDescription());
                        statement.setString(3, subreddit.getOwner());
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                deleteOrphanSubreddits(connection, subreddits);
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            logDbFailure("subreddits", e);
        }
    }

    public static void syncPosts(List<Post> posts) {
        try (Connection connection = DataBaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                upsertPosts(connection, posts);
                syncCommentsForPosts(connection, posts);
                deleteOrphanPosts(connection, posts);
                resetSequence(connection, "posts");
                resetSequence(connection, "comments");
                connection.commit();
            } catch (Exception e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (Exception e) {
            logDbFailure("posts", e);
        }
    }

    public static void upsertPostVote(String username, int postId, int voteType) {
        String sql = "INSERT INTO post_votes (account_id, post_id, vote_type) "
                + "VALUES ((SELECT id FROM accounts WHERE username = ?), ?, ?) "
                + "ON CONFLICT (account_id, post_id) DO UPDATE SET vote_type = EXCLUDED.vote_type";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setInt(2, postId);
            statement.setInt(3, voteType);
            statement.executeUpdate();
        } catch (Exception e) {
            logDbFailure("post_votes", e);
        }
    }

    public static void removePostVote(String username, int postId, int voteType) {
        String sql = "DELETE FROM post_votes "
                + "WHERE account_id = (SELECT id FROM accounts WHERE username = ?) "
                + "AND post_id = ? AND vote_type = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setInt(2, postId);
            statement.setInt(3, voteType);
            statement.executeUpdate();
        } catch (Exception e) {
            logDbFailure("post_votes", e);
        }
    }

    public static void upsertCommentVote(String username, int commentId, int voteType) {
        String sql = "INSERT INTO comment_votes (account_id, comment_id, vote_type) "
                + "VALUES ((SELECT id FROM accounts WHERE username = ?), ?, ?) "
                + "ON CONFLICT (account_id, comment_id) DO UPDATE SET vote_type = EXCLUDED.vote_type";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setInt(2, commentId);
            statement.setInt(3, voteType);
            statement.executeUpdate();
        } catch (Exception e) {
            logDbFailure("comment_votes", e);
        }
    }

    public static void removeCommentVote(String username, int commentId, int voteType) {
        String sql = "DELETE FROM comment_votes "
                + "WHERE account_id = (SELECT id FROM accounts WHERE username = ?) "
                + "AND comment_id = ? AND vote_type = ?";
        try (Connection connection = DataBaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setInt(2, commentId);
            statement.setInt(3, voteType);
            statement.executeUpdate();
        } catch (Exception e) {
            logDbFailure("comment_votes", e);
        }
    }

    private static void upsertPosts(Connection connection, List<Post> posts) throws SQLException {
        String sql = "INSERT INTO posts (id, title, content, author_id, subreddit_id) "
                + "VALUES (?, ?, ?, (SELECT id FROM accounts WHERE username = ?), "
                + "(SELECT id FROM subreddits WHERE name = ?)) "
                + "ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, content = EXCLUDED.content, "
                + "author_id = EXCLUDED.author_id, subreddit_id = EXCLUDED.subreddit_id";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Post post : posts) {
                statement.setInt(1, post.getId());
                statement.setString(2, post.getTitle());
                statement.setString(3, post.getContent());
                statement.setString(4, post.getAuthor());

                String subredditName = post.getSubredditName();
                if(subredditName != null && !subredditName.startsWith("r/")){
                    subredditName = "r/" + subredditName;
                }
                statement.setString(5, subredditName);
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private static void syncCommentsForPosts(Connection connection, List<Post> posts) throws SQLException {
        Set<Integer> keepCommentIds = new HashSet<>();
        String upsertSql = "INSERT INTO comments (id, content, post_id, author_id, parent_comment_id) "
                + "VALUES (?, ?, ?, (SELECT id FROM accounts WHERE username = ?), ?) "
                + "ON CONFLICT (id) DO UPDATE SET content = EXCLUDED.content, post_id = EXCLUDED.post_id, "
                + "author_id = EXCLUDED.author_id, parent_comment_id = EXCLUDED.parent_comment_id";

        try (PreparedStatement statement = connection.prepareStatement(upsertSql)) {
            for (Post post : posts) {
                upsertCommentTree(statement, post.getComments(), post.getId(), null, keepCommentIds);
            }
            statement.executeBatch();
        }

        if (posts.isEmpty()) {
            return;
        }

        StringBuilder deleteSql = new StringBuilder(
                "DELETE FROM comments WHERE post_id IN ("
        );
        for (int i = 0; i < posts.size(); i++) {
            if (i > 0) {
                deleteSql.append(',');
            }
            deleteSql.append('?');
        }
        if (keepCommentIds.isEmpty()) {
            deleteSql.append(')');
        } else {
            deleteSql.append(") AND id NOT IN (");
            for (int i = 0; i < keepCommentIds.size(); i++) {
                if (i > 0) {
                    deleteSql.append(',');
                }
                deleteSql.append('?');
            }
            deleteSql.append(')');
        }

        try (PreparedStatement statement = connection.prepareStatement(deleteSql.toString())) {
            int index = 1;
            for (Post post : posts) {
                statement.setInt(index++, post.getId());
            }
            for (Integer commentId : keepCommentIds) {
                statement.setInt(index++, commentId);
            }
            statement.executeUpdate();
        }
    }

    private static void upsertCommentTree(PreparedStatement statement,
                                          List<Comment> comments,
                                          int postId,
                                          Integer parentCommentId,
                                          Set<Integer> keepCommentIds) throws SQLException {
        if (comments == null) {
            return;
        }
        for (Comment comment : comments) {
            keepCommentIds.add(comment.getId());
            statement.setInt(1, comment.getId());
            statement.setString(2, comment.getText());
            statement.setInt(3, postId);
            statement.setString(4, comment.getAuthor());
            if (parentCommentId == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, parentCommentId);
            }
            statement.addBatch();
            upsertCommentTree(statement, comment.getReplies(), postId, comment.getId(), keepCommentIds);
        }
    }

    private static void deleteOrphanPosts(Connection connection, List<Post> posts) throws SQLException {
        if (posts.isEmpty()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM posts");
            }
            return;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM posts WHERE id NOT IN (");
        for (int i = 0; i < posts.size(); i++) {
            if (i > 0) {
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(')');

        try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < posts.size(); i++) {
                statement.setInt(i + 1, posts.get(i).getId());
            }
            statement.executeUpdate();
        }
    }

    private static void deleteOrphanAccounts(Connection connection, List<Account> accounts) throws SQLException{
        if(accounts.isEmpty()){
            try(Statement statement = connection.createStatement()){
                statement.executeUpdate("DELETE FROM accounts");
            }
            return;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM accounts WHERE username NOT IN (");
        for(int i = 0; i < accounts.size(); i++){
            if(i > 0){
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(')');

        try(PreparedStatement statement = connection.prepareStatement(sql.toString())){
            for(int i = 0; i < accounts.size(); i++){
                statement.setString(i + 1, accounts.get(i).getUsername());
            }
            statement.executeUpdate();
        }
    }

    private static void deleteOrphanSubreddits(Connection connection, List<Subreddit> subreddits) throws SQLException{
        if(subreddits.isEmpty()){
            try(Statement statement = connection.createStatement()){
                statement.executeUpdate("DELETE FROM subreddits");
            }
            return;
        }

        StringBuilder sql = new StringBuilder("DELETE FROM subreddits WHERE name NOT IN (");
        for(int i = 0; i < subreddits.size(); i++){
            if(i > 0){
                sql.append(',');
            }
            sql.append('?');
        }
        sql.append(')');

        try(PreparedStatement statement = connection.prepareStatement(sql.toString())){
            for(int i = 0; i < subreddits.size(); i++){
                statement.setString(i + 1, subreddits.get(i).getName());
            }
            statement.executeUpdate();
        }
    }

    private static void resetSequence(Connection connection, String tableName) throws SQLException {
        String sql = "SELECT setval(pg_get_serial_sequence('" + tableName + "', 'id'), "
                + "COALESCE((SELECT MAX(id) FROM " + tableName + "), 1))";
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private static void logDbFailure(String entity, Exception e) {
        String message = "Database dual-write failed for " + entity + ": " + e.getMessage();
        System.err.println(message);
        Logger.getInstance().log(LogLevel.ERROR, message);
    }
}
