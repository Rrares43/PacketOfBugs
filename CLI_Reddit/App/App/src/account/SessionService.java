package account;

public class SessionService {
    private static final String GUEST_USERNAME = "guest";
    private String currentUsername = GUEST_USERNAME;

    public void login(String username) {
        this.currentUsername = username;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public boolean isLoggedIn() {
        return !GUEST_USERNAME.equals(currentUsername);
    }

    public void logout() {
        this.currentUsername = GUEST_USERNAME;
    }
}
