package post.service;

import account.SessionService;
import persistence.RedditApiClient;
import post.model.Post;
import post.model.PostVote;
import post.repository.PostRepo;

import java.util.Optional;

public class PostVoteServiceImpl implements PostVoteService {
    private final SessionService sessionService;

    public PostVoteServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public String upvote(int postId, int choice) {
        return handleVote(postId, choice, true);
    }

    @Override
    public String downvote(int postId, int choice) {
        return handleVote(postId, choice, false);
    }

    private String handleVote(int postId, int choice, boolean isUpvote) {
        if(!sessionService.isLoggedIn()){
            return "You must be logged in to vote.";
        }

        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            return RedditApiClient.votePost(postId, authorId, isUpvote, choice);
        }
        catch (Exception e){
            return "Error: " + e.getMessage();
        }
    }

}