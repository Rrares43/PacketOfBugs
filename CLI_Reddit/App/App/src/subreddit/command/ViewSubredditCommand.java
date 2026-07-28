package subreddit.command;

import io.FormattedOutputWriter;
import post.PostInteractionController;
import post.PostView;
import post.model.Post;
import post.repository.PostRepository;
import subreddit.Subreddit;
import subreddit.repository.SubredditRepository;
import io.OutputWriter;
import io.StringReader;

import java.util.List;

public class ViewSubredditCommand implements SubredditCommand {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final PostRepository postRepo;

    private final PostView postView;

    public ViewSubredditCommand(StringReader stringReader, OutputWriter output, PostRepository postRepo, PostView postView) {
        this.stringReader = stringReader;
        this.output = output;
        this.postRepo = postRepo;
        this.postView = postView;
    }

    @Override
    public void execute(){
        System.out.println("Choose a subreddit to view:");
        List<Subreddit> subreddits = SubredditRepository.loadSubreddits();
        String sub = stringReader.readString("Enter subreddit name: ");
        boolean found = false;
        if(!sub.startsWith("r/")){
            sub = "r/" + sub;
        }
        for(Subreddit s : subreddits){
            if(s.getName().equals(sub)){
                found = true;
                output.write("Subreddit: " + s.getName());
                output.write(s.getDescription());
                output.write("Owner: " + s.getOwner());

                List<Post> posts = postRepo.findPostsBySubreddit(s.getName());
                if (posts.isEmpty()) {
                    output.write("No posts found in " + s.getName() + ".");
                    return;
                }

                output.write("\nPosts in " + s.getName() + ":");
                for (Post post : posts) {
                    output.write("ID: " + post.getId() + " | Title: " + post.getTitle());
                }

                int postID = Integer.parseInt(stringReader.readString("Enter the ID of the post you want to interact with:"));
                Post foundPost = null;
                for (Post post : posts) {
                    if (post.getId() == postID) {
                        foundPost = post;
                    }
                }
                if (foundPost != null) {
                    postView.displayPost(foundPost);
                    output.write("\n");
                } else {
                    output.write("Error: Post with ID " + postID + " does not exist in " + s.getName() + ".");
                }
            }
        }
        if(!found){
            output.write("Subreddit doesn't exist");
        }
    }
}
