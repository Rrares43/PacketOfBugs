package account.operations;

import account.SessionService;
import account.dto.AccountApiDtos;
import com.google.gson.Gson;
import io.OutputWriter;
import persistence.RedditApiClient;

import java.net.http.HttpResponse;

public class AccountInfo {
    private final SessionService sessionService;
    private final OutputWriter output;
    private final Gson gson;

    public AccountInfo(SessionService sessionService, OutputWriter output) {
        this.sessionService = sessionService;
        this.output = output;
        this.gson = RedditApiClient.gson();
    }

    public void checkCurrentUser() {
        if (!sessionService.isLoggedIn()) {
            output.write("Logged in as GUEST");
            return;
        }

        String currentUser = sessionService.getCurrentUsername();
        try {
            HttpResponse<String> response = RedditApiClient.getAccountRaw(currentUser);
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                AccountApiDtos.AccountResponse account =
                        gson.fromJson(response.body(), AccountApiDtos.AccountResponse.class);
                String email = account.email != null ? account.email : sessionService.getCurrentEmail();
                showUser(account.username, email);
            } else if (RedditApiClient.isClientError(status)) {
                showUser(currentUser, sessionService.getCurrentEmail());
            } else {
                output.write("Could not fetch account. Status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            showUser(currentUser, sessionService.getCurrentEmail());
        }
    }

    private void showUser(String username, String email) {
        output.write("Logged in as:");
        output.write("Username: " + username);
        output.write("Email: " + (email == null || email.isBlank() ? "(not stored on server)" : email));
    }
}