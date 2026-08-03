package account.operations;

import account.SessionService;
import io.OutputWriter;
import io.StringReader;
import api.RedditApiClient;

import java.net.http.HttpResponse;

public class AccountDeleter {
    private final StringReader stringReader;
    private final OutputWriter output;
    private final SessionService sessionService;

    public AccountDeleter(StringReader stringReader, OutputWriter output, SessionService sessionService) {
        this.stringReader = stringReader;
        this.output = output;
        this.sessionService = sessionService;
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
            HttpResponse<String> response = RedditApiClient.deleteAccountRaw(username);
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
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
