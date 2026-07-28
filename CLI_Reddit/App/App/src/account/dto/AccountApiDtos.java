package account.dto;

/**
 * Gson DTOs mirroring Spring Boot {@code AccountDto} request/response shapes.
 */
public final class AccountApiDtos {

    private AccountApiDtos() {
    }

    public static class RegistrationRequest {
        public String username;
        public String email;
        public String password;

        public RegistrationRequest(String username, String email, String password) {
            this.username = username;
            this.email = email;
            this.password = password;
        }
    }

    public static class LoginRequest {
        public String username;
        public String password;

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    public static class ChangePasswordRequest {
        public String username;
        public String oldPassword;
        public String newPassword;

        public ChangePasswordRequest(String username, String oldPassword, String newPassword) {
            this.username = username;
            this.oldPassword = oldPassword;
            this.newPassword = newPassword;
        }
    }

    public static class AccountResponse {
        public Long id;
        public String username;
        public String email;
    }
}
