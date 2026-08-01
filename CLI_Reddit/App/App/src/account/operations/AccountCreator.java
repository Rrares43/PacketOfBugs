package account.operations;

import account.dto.AccountApiDtos;
import account.verification.EmailVerification;
import account.verification.NameVerification;
import account.verification.PasswordVerification;
import com.google.gson.Gson;
import io.OutputWriter;
import io.StringReader;
import persistence.RedditApiClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AccountCreator {
    private static final String REGISTER_URL = RedditApiClient.getBaseUrl() + "/api/accounts/register";

    private final StringReader stringReader;
    private final OutputWriter output;
    private final HttpClient httpClient;
    private final Gson gson;

    public AccountCreator(StringReader stringReader, OutputWriter output) {
        this.stringReader = stringReader;
        this.output = output;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = RedditApiClient.gson();
    }

    public void createAccount() {
        String answer = stringReader.readString("Create an account? (y/n)");
        if (!answer.equalsIgnoreCase("y")) {
            output.write("Account creation cancelled.");
            return;
        }

        String username;
        while (true) {
            username = stringReader.readString("Enter username: (or 0 to cancel)");
            if (username.equals("0")) {
                return;
            }
            if (NameVerification.verify(username)) {
                break;
            }
            output.write("Invalid username format!");
        }

        String email;
        while (true) {
            email = stringReader.readString("Enter email: (or 0 to cancel)");
            if (email.equals("0")) {
                return;
            }
            if (EmailVerification.verify(email)) {
                break;
            }
            output.write("Invalid email format!");
        }

        String password;
        while (true) {
            output.write("Password must be at least 8 characters long and contain at least one number, one uppercase letter, one lowercase letter, and one special character.");
            password = stringReader.readString("Enter Password (or 0 to cancel): ");
            if (password.equals("0")) {
                return;
            }
            if (PasswordVerification.verify(password)) {
                break;
            }
            output.write("Weak password. Please try again.");
        }

        try {
            HttpResponse<String> response = RedditApiClient.registerAccountRaw(username, email, password);
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                output.write("Account saved successfully!");
            } else if (RedditApiClient.isClientError(status)) {
                output.write(response.body());
            } else {
                output.write("Account creation failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
        }
    }
}
