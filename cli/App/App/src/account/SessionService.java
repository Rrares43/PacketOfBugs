package account;

public class SessionService {
    private static final String GUEST_USERNAME = "guest";
    private String currentUsername = GUEST_USERNAME;
    private String currentEmail;
    private Long currentAccountId;
    private String jwtToken;

    public void login(String username, String email, Long accountId, String jwtToken) {
        this.currentUsername = username;
        this.currentEmail = email;
        this.currentAccountId = accountId;
        this.jwtToken = jwtToken;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentEmail() {
        return currentEmail;
    }

    public Long getCurrentAccountId() {
        return currentAccountId;
    }

    public boolean isLoggedIn() {
        return !GUEST_USERNAME.equals(currentUsername);
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public void logout() {
        this.currentUsername = GUEST_USERNAME;
        this.currentEmail = null;
        this.currentAccountId = null;
        this.jwtToken = null;
    }
}
