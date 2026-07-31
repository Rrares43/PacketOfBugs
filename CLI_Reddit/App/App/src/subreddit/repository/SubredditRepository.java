package subreddit.repository;

import persistence.ApiMapper;
import persistence.RedditApiClient;
import subreddit.Subreddit;
import util.SubredditNames;

import java.util.List;
import java.util.Optional;

public class SubredditRepository {
    public static List<Subreddit> loadSubreddits(){
        return ApiMapper.toSubredditList(RedditApiClient.getAllSubreddits());
    }

    public static Optional<Subreddit> findByName(String name){
        String normalizedName = SubredditNames.normalize(name);
        return RedditApiClient.getSubredditByName(normalizedName).map(ApiMapper::toSubreddit);
    }

    public static void listSubsMadebyUser(String user){
        List<Subreddit> subs = ApiMapper.toSubredditList(RedditApiClient.getSubredditsByCreator(user));
        for(Subreddit sub : subs){
            System.out.println(sub.getName());
        }
    }

    public static void saveSubreddit(Subreddit subreddit){
        try{
            long creatorId = RedditApiClient.resolveAccountId(subreddit.getOwner());
            RedditApiClient.createSubreddit(subreddit.getName(), subreddit.getDescription(), creatorId);
        }
        catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }

}