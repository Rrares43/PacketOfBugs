package account.operations;

import account.SessionService;
import account.dto.AccountApiDtos;
import account.verification.PasswordVerification;
import com.google.gson.Gson;
import io.OutputWriter;
import io.StringReader;
import logger.LogLevel;
import logger.Logger;
import persistence.RedditApiClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PasswordChanger {
    private static final String PASSWORD_URL = RedditApiClient.getBaseUrl() + "/api/accounts/password";

    private final StringReader stringReader;
    private final OutputWriter output;
    private final SessionService sessionService;
    private final HttpClient httpClient;
    private final Gson gson;

    public PasswordChanger(StringReader stringReader, OutputWriter output, SessionService sessionService) {
        this.stringReader = stringReader;
        this.output = output;
        this.sessionService = sessionService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = RedditApiClient.gson();
    }

    public void ChangePassword() {
        if (!sessionService.isLoggedIn()) {
            output.write("You must be logged in to change your password.");
            return;
        }

        String username = sessionService.getCurrentUsername();
        String oldPassword = stringReader.readString("Enter current password: ");

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
            AccountApiDtos.ChangePasswordRequest payload =
                    new AccountApiDtos.ChangePasswordRequest(username, oldPassword, newPassword);
            String jsonPayload = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(PASSWORD_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                output.write(response.body());
                Logger.getInstance().log(LogLevel.INFO, "Password changed for " + username);
            } else if (RedditApiClient.isClientError(status)) {
                output.write(response.body());
                Logger.getInstance().log(LogLevel.WARNING, "Password change failed: " + response.body());
            } else {
                output.write("Password change failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
            Logger.getInstance().log(LogLevel.ERROR, "Password change connection error: " + e.getMessage());
        }
    }
}
