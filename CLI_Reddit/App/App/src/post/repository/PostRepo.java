package post.repository;

import account.SessionService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import persistence.ApiMapper;
import persistence.RedditApiClient;
import post.model.Comment;
import post.model.Post;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostRepo implements PostRepository {
    private final SessionService sessionService;

    public PostRepo(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public Post findPostById(int postId) {
        Optional<JsonObject> postJson = RedditApiClient.getPost(postId);
        if(postJson.isPresent()){
            JsonArray comments = RedditApiClient.getComments(postId);
            return ApiMapper.toPostWithComments(postJson.get(), comments);
        }
        return null;
    }

    @Override
    public List<Post> findAllPosts() {
        return ApiMapper.toPostList(RedditApiClient.getAllPosts());
    }

    @Override
    public String getCurrentUser() {
        return sessionService.getCurrentUsername();
    }

    @Override
    public List<Post> findPostsBySubreddit(String subredditName){
        return ApiMapper.toPostList(RedditApiClient.getPostsBySubreddit(subredditName));
    }

    @Override
    public void addPost(Post post) {
        try{
            long authorId = RedditApiClient.resolveAccountId(post.getAuthor());
            RedditApiClient.createPost(post.getTitle(), post.getContent(), authorId, post.getSubredditName());
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Override
    public boolean removePost(int postId){
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.deletePost(postId, authorId);
            return true;
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        return false;
    }

    @Override
    public int getNextCommentId() { return 0; }

    @Override
    public void saveToFile() {}
}
