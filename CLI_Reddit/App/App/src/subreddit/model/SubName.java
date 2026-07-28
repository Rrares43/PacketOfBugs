package subreddit.model;

import logger.LogLevel;
import logger.Logger;
import post.validator.IsNotBlank;

import java.util.Scanner;

public class SubName implements SubredditData {
    public static String ask(){
        while(true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("Enter subreddit name:");
            String subredditName = sc.nextLine();

            IsNotBlank isNotBlank = new IsNotBlank();
            if (!isNotBlank.isValid(subredditName)) {
                System.out.println("Error: Subreddit name cannot be empty!");
                Logger.getInstance().log(LogLevel.ERROR,"Error: Subreddit name cannot be empty!");
            }
            else if(subredditName.equals("0")) {
                System.out.println("Back to menu");
                return "0";
            }
            else {
                return subredditName;
            }
        }
    }
}
