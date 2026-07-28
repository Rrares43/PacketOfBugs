package subreddit.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import logger.LogLevel;
import logger.Logger;
import persistence.DataPaths;
import persistence.DatabaseSync;
import subreddit.Subreddit;
import util.SubredditNames;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JSON-primary subreddit store ({@code CLI_Reddit/App/data/subreddits.json}).
 * Remote dual-write runs only on save/update/delete via {@link DatabaseSync}.
 */
public class SubredditRepository {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return DataPaths.resolveDataFile("subreddits.json");
    }

    public static List<Subreddit> loadSubreddits() {
        Path path = file();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Type listType = new TypeToken<ArrayList<Subreddit>>(){}.getType();
            List<Subreddit> subreddits = gson.fromJson(reader, listType);
            return subreddits != null ? subreddits : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR, "Error reading subreddits.json from " + path);
            return new ArrayList<>();
        }
    }

    public static Optional<Subreddit> findByName(String name) {
        String normalized = SubredditNames.normalize(name);
        for (Subreddit subreddit : loadSubreddits()) {
            if (SubredditNames.normalize(subreddit.getName()).equals(normalized)) {
                return Optional.of(subreddit);
            }
        }
        return Optional.empty();
    }

    public static void listSubsMadebyUser(String user) {
        for (Subreddit sub : loadSubreddits()) {
            if (sub.getOwner().equals(user)) {
                System.out.println(sub.getName());
            }
        }
    }

    public static void writeSubreddits(List<Subreddit> subreddits) {
        Path path = file();
        try {
            DataPaths.ensureParent(path);
            try (Writer fileWriter = Files.newBufferedWriter(path)) {
                gson.toJson(subreddits, fileWriter);
            }
        } catch (IOException e) {
            System.out.println("Error: Could not write to file");
            Logger.getInstance().log(LogLevel.ERROR, "Could not write subreddits.json: " + e.getMessage());
            return;
        }
        DatabaseSync.syncSubreddits(subreddits);
    }

    public static void saveSubreddit(Subreddit subreddit) {
        try {
            List<Subreddit> subreddits = loadSubreddits();
            subreddits.add(subreddit);
            writeSubreddits(subreddits);

            System.out.println("Subreddit saved successfully!");
            Logger.getInstance().log(LogLevel.INFO, "Subreddit saved successfully!");
        } catch (Exception e) {
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR, "Error saving subreddit");
        }
    }
}
