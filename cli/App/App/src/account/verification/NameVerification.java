package account.verification;

import api.RedditApiClient;

public class NameVerification  {

    public static boolean verify(String name) {
        if (name.length() < 3 || name.length() > 20) {
            return false;
        }

        boolean isUsernameAvailable = RedditApiClient.isUsernameAvailable(name);
        if (!isUsernameAvailable) {
            System.out.println("Username is already taken");
            return false;
        }
        return true;
    }
}
