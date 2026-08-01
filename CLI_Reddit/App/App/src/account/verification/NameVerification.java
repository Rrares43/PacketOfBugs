package account.verification;

import account.Account;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import persistence.RedditApiClient;

import java.util.List;

public class NameVerification implements AccountVerifier {
    public NameVerification(){}

    public static boolean verify(String name){
        if(name.length() < 3 || name.length() > 20){
            return false;
        }

        boolean isUsernameAvailable = RedditApiClient.isUsernameAvailable(name);
        if(!isUsernameAvailable){
            System.out.println("Username is already taken");
            return false;
        }
        return true;
    }
}
