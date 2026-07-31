package post.service;

import account.SessionService;
import persistence.RedditApiClient;
import post.model.Comment;
import post.model.CommentVote;
import post.repository.PostRepo;
import logger.Logger;
import logger.LogLevel;

import java.util.Optional;

public class CommentVoteServiceImpl implements CommentVoteService {
    private final SessionService sessionService;
    public CommentVoteServiceImpl(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @Override
    public String upvoteComment(int postId, int commentId, int choice) {
        return handleVote(postId, commentId, true, choice);
    }

    @Override
    public String downvoteComment(int postId, int commentId, int choice) {
        return handleVote(postId, commentId, false, choice);
    }

    private String handleVote(int postId, int commentId, boolean isUpvote, int choice) {
        if(!sessionService.isLoggedIn()){
            return "You must be logged in to vote.";
        }
        try{
            long authorId = RedditApiClient.resolveAccountId(sessionService.getCurrentUsername());
            return RedditApiClient.voteComment(postId, commentId, authorId, isUpvote, choice);
        }
        catch (Exception e){
            return "Error: " + e.getMessage();
        }
    }
}