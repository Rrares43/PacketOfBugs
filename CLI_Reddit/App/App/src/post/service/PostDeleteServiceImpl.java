package post.service;

import account.SessionService;
import persistence.RedditApiClient;
import post.model.Post;
import post.repository.PostRepository;

public class PostDeleteServiceImpl implements PostDeleteService {
    private final SessionService sessionService;
    public PostDeleteServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public void deletePost(int postId) {
        if(!sessionService.isLoggedIn()){
            throw new SecurityException("You must be logged in to delete a post.");
        }

        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            RedditApiClient.deletePost(postId, authorId);
        }
        catch (Exception e){
            throw new IllegalArgumentException("Error deleting post: " + e.getMessage());
        }
    }
}
