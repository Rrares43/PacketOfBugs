package post.repository;

import account.SessionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import persistence.DataPaths;
import persistence.DatabaseSync;
import post.model.Comment;
import post.model.Post;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PostRepo implements PostRepository {
    private final List<Post> posts;
    private final Path dbFile;
    private final Gson gson;
    private final SessionService sessionService;
    private int nextCommentId = 1;

    public PostRepo(SessionService sessionService) {
        this.sessionService = sessionService;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.dbFile = DataPaths.resolveDataFile("reddit_database.json");
        this.posts = loadFromFile();
        initializeNextCommentId();
        assignMissingPostIds();
    }

    private void initializeNextCommentId() {
        int maxId = 0;
        for (Post post : posts) {
            maxId = Math.max(maxId, findMaxCommentId(post.getComments()));
        }
        nextCommentId = maxId + 1;
    }

    private void assignMissingPostIds() {
        for (Post post : posts) {
            assignPostIdRecursive(post.getComments(), post.getId());
        }
    }

    private void assignPostIdRecursive(List<Comment> comments, int postId) {
        if (comments == null) {
            return;
        }
        for (Comment comment : comments) {
            if (comment.getPostId() == 0) {
                comment.setPostId(postId);
            }
            assignPostIdRecursive(comment.getReplies(), postId);
        }
    }

    private int findMaxCommentId(List<Comment> comments) {
        int max = 0;
        if (comments == null) {
            return max;
        }
        for (Comment comment : comments) {
            max = Math.max(max, comment.getId());
            max = Math.max(max, findMaxCommentId(comment.getReplies()));
        }
        return max;
    }

    private List<Post> loadFromFile() {
        if (!Files.exists(dbFile)) {
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(dbFile)) {
            Type listType = new TypeToken<ArrayList<Post>>(){}.getType();
            List<Post> loadedData = gson.fromJson(reader, listType);
            return loadedData != null ? loadedData : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error reading JSON database: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private void writeJson() {
        try {
            DataPaths.ensureParent(dbFile);
            try (Writer writer = Files.newBufferedWriter(dbFile)) {
                gson.toJson(this.posts, writer);
            }
        } catch (Exception e) {
            System.err.println("Error saving to JSON: " + e.getMessage());
            throw new IllegalStateException("Could not write to file: " + dbFile, e);
        }
    }

    @Override
    public void saveToFile() {
        try {
            writeJson();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
            return;
        }
        DatabaseSync.syncPosts(this.posts);
        persistLocalState();
    }

    /** Writes in-memory posts (including ID remaps) to the canonical JSON file without re-syncing. */
    public void persistLocalState() {
        try {
            writeJson();
        } catch (IllegalStateException e) {
            System.err.println(e.getMessage());
        }
        initializeNextCommentId();
    }

    @Override
    public Post findPostById(int postId) {
        for (Post p : this.posts) {
            if (p.getId() == postId) {
                return p;
            }
        }
        return null;
    }

    @Override
    public List<Post> findAllPosts() {
        return this.posts;
    }

    @Override
    public List<Post> findPostsBySubreddit(String subredditName) {
        List<Post> result = new ArrayList<>();
        for (Post post : this.posts) {
            if (post.getSubredditName() != null && post.getSubredditName().equals(subredditName)) {
                result.add(post);
            }
        }
        return result;
    }

    @Override
    public int getNextCommentId() {
        return nextCommentId++;
    }

    @Override
    public String getCurrentUser() {
        return sessionService.getCurrentUsername();
    }

    @Override
    public void addPost(Post post) {
        this.posts.add(post);
        saveToFile();
    }

    @Override
    public boolean removePost(int postId) {
        boolean removed = false;

        for (int i = posts.size() - 1; i >= 0; i--) {
            if (posts.get(i).getId() == postId) {
                posts.remove(i);
                removed = true;
            }
        }
        if (removed) {
            saveToFile();
        }

        return removed;
    }

    public Comment findCommentById(int postId, int commentId) {
        Post post = findPostById(postId);
        if (post == null) {
            return null;
        }
        return searchInComments(post.getComments(), commentId);
    }

    public boolean removeComment(int postId, int commentId) {
        Post post = findPostById(postId);
        if (post == null) {
            return false;
        }
        return removeFromList(post.getComments(), commentId);
    }

    private boolean removeFromList(List<Comment> comments, int commentId) {
        if (comments == null) {
            return false;
        }
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getId() == commentId) {
                comments.remove(i);
                return true;
            }
            if (removeFromList(comments.get(i).getReplies(), commentId)) {
                return true;
            }
        }
        return false;
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
