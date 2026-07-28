package account.operations;

import account.SessionService;
import account.dto.AccountApiDtos;
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

public class AccountLogin {
    private static final String LOGIN_URL = RedditApiClient.getBaseUrl() + "/api/accounts/login";

    private final StringReader stringReader;
    private final SessionService sessionService;
    private final OutputWriter output;
    private final HttpClient httpClient;
    private final Gson gson;

    public AccountLogin(StringReader stringReader, SessionService sessionService, OutputWriter output) {
        this.stringReader = stringReader;
        this.sessionService = sessionService;
        this.output = output;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.gson = RedditApiClient.gson();
    }

    public void Login() {
        String username = stringReader.readString("Enter username: ");
        String password = stringReader.readString("Enter password: ");

        try {
            AccountApiDtos.LoginRequest payload = new AccountApiDtos.LoginRequest(username, password);
            String jsonPayload = gson.toJson(payload);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(LOGIN_URL))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                AccountApiDtos.AccountResponse account =
                        gson.fromJson(response.body(), AccountApiDtos.AccountResponse.class);
                sessionService.login(account.username, account.email, account.id);
                output.write("Login Successful. Logged in as: " + account.username);
                Logger.getInstance().log(LogLevel.INFO, account.username + " has logged in.");
            } else if (RedditApiClient.isClientError(status)) {
                output.write(response.body());
                Logger.getInstance().log(LogLevel.WARNING, "Login failed: " + response.body());
            } else {
                output.write("Login failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
            Logger.getInstance().log(LogLevel.ERROR, "Login connection error: " + e.getMessage());
        }
    }
}
