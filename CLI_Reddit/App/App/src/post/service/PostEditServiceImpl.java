package post.service;

import account.SessionService;
import persistence.RedditApiClient;
import post.model.Post;
import post.repository.PostRepository;

import java.nio.file.SecureDirectoryStream;

public class PostEditServiceImpl implements PostEditService{
    private final SessionService sessionService;

    public PostEditServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void editPost(int postId, String newTitle, String newContent) {
        if(!sessionService.isLoggedIn()){
            throw new SecurityException("You must be logged in to edit a post.");
        }
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.editPost(postId, newTitle, newContent, authorId);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error editing post: " + e.getMessage());
        }
    }
}
