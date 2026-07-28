package subreddit.model;

import logger.LogLevel;
import logger.Logger;
import post.validator.IsNotBlank;

import java.util.Scanner;

public class SubDescription implements SubredditData {
    public static String ask(){
        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter subreddit description:");
            String subredditDescription = sc.nextLine();

            IsNotBlank isNotBlank = new IsNotBlank();
            if (!isNotBlank.isValid(subredditDescription)) {
                System.out.println("Error: Subreddit description cannot be empty!");
                Logger.getInstance().log(LogLevel.ERROR,"Error: Subreddit description cannot be empty!");
            }
            else if(subredditDescription.equals("0")) {
                System.out.println("Back to menu");
                return "0";
            }
            else {
                return subredditDescription;
            }
        }
    }
}
