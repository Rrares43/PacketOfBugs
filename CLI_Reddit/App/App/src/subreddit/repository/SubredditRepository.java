package subreddit.repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import logger.Logger;
import logger.LogLevel;
import persistence.DatabaseSync;
import subreddit.Subreddit;

public class SubredditRepository {
    private static final String FILE_NAME = "App/data/subreddits.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static List<Subreddit> loadSubreddits() {
        File file = new File(FILE_NAME);
        if(!file.exists()){
            return new ArrayList<>();
        }
        try(FileReader reader = new FileReader(file)){
            Type listType = new TypeToken<ArrayList<Subreddit>>(){}.getType();
            List<Subreddit> subreddits = gson.fromJson(reader, listType);
            return subreddits != null ? subreddits : new ArrayList<>();
        }
        catch(IOException e){
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR,"Error");
            return new ArrayList<>();
        }
    }

    public static void listSubsMadebyUser(String user){
        List<Subreddit> subreddits = loadSubreddits();
        for(Subreddit sub : subreddits){
            if(sub.getOwner().equals(user)){
                System.out.println(sub.getName());
            }
        }
    }

    public static void writeSubreddits(List<Subreddit> subreddits){
        try(FileWriter fileWriter = new FileWriter(FILE_NAME)){
            gson.toJson(subreddits, fileWriter);
        }
        catch (IOException e){
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR,"Error");
            return;
        }
        DatabaseSync.syncSubreddits(subreddits);
    }

    public static void saveSubreddit(Subreddit subreddit){
        try{
            List<Subreddit> subreddits = loadSubreddits();
            subreddits.add(subreddit);
            writeSubreddits(subreddits);

            System.out.println("Subreddit saved successfully!");
            Logger.getInstance().log(LogLevel.INFO,"Subreddit saved successfully!");
        }
        catch(Exception e){
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR,"Error");
        }
    }

}
