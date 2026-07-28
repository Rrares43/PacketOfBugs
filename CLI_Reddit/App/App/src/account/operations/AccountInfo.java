package account.operations;

import account.Account;

public class AccountInfo {
    public static void checkUser(Account account){
        System.out.println("Logged in as:");
        System.out.println("Username: " + account.getUsername());
        System.out.println("Email: " + account.getEmail());
    }
}
