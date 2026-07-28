package account.operations;

import account.SessionService;
import io.OutputWriter;
import io.StringReader;
import persistence.RedditApiClient;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class AccountDeleter {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final SessionService sessionService;
    private final HttpClient httpClient;

    public AccountDeleter(StringReader stringReader, OutputWriter output, SessionService sessionService) {
        this.stringReader = stringReader;
        this.output = output;
        this.sessionService = sessionService;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    public void DeleteAccount() {
        if (!sessionService.isLoggedIn()) {
            output.write("You must be logged in to delete your account.");
            return;
        }

        String choice = stringReader.readString(
                "Are you sure you want to delete your account? (y/n) (this will automatically log you out)");
        if (!choice.equalsIgnoreCase("y")) {
            output.write("Account deletion cancelled.");
            return;
        }

        String username = sessionService.getCurrentUsername();
        try {
            String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8).replace("+", "%20");
            String url = RedditApiClient.getBaseUrl() + "/api/accounts/" + encodedUsername;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                account.repository.AccountRepository.deleteLocalAccount(username);
                output.write(response.body());
                sessionService.logout();
            } else if (RedditApiClient.isClientError(status)) {
                output.write(response.body());
            } else {
                output.write("Account deletion failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
        }
    }
}
