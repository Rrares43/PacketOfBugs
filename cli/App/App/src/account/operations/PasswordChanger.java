package account.operations;

import account.SessionService;
import account.verification.EmailVerification;
import account.verification.PasswordVerification;
import io.OutputWriter;
import io.StringReader;
import api.RedditApiClient;

import java.net.http.HttpResponse;

public class PasswordChanger {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final SessionService sessionService;

    public PasswordChanger(StringReader stringReader, OutputWriter output, SessionService sessionService) {
        this.stringReader = stringReader;
        this.output = output;
        this.sessionService = sessionService;
    }

    public void ChangePassword() {
        if (!sessionService.isLoggedIn()) {
            output.write("You must be logged in to change your password.");
            return;
        }

        String username = sessionService.getCurrentUsername();
        String currentPassword = stringReader.readString("Enter current password: ");
        String newPassword;
        while (true) {
            output.write("Password must be at least 8 characters long and contain at least one number, one uppercase letter, one lowercase letter, and one special character.");
            newPassword = stringReader.readString("Enter new password (or 0 to cancel): ");
            if (newPassword.equals("0")) {
                output.write("Password change cancelled.");
                return;
            }
            if (PasswordVerification.verify(newPassword)) {
                break;
            }
            output.write("Weak password. Please try again.");
        }

        try {
            HttpResponse<String> response = RedditApiClient.changePasswordRaw(currentPassword, newPassword);
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                output.write(response.body());
            } else if (RedditApiClient.isClientError(status)) {
                output.write(response.body());
            } else {
                output.write("Password change failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
        }
    }
}
