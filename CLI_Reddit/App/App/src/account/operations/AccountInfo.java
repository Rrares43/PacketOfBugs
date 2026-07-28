package account.operations;

public class AccountInfo {
    public static void checkUser(String username, String email) {
        System.out.println("Logged in as:");
        System.out.println("Username: " + username);
        System.out.println("Email: " + (email == null || email.isBlank() ? "(not stored on server)" : email));
    }
}
