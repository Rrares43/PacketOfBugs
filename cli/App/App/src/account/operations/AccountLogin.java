package account.operations;

import account.SessionService;
import account.dto.AccountApiDtos;
import com.google.gson.Gson;
import io.OutputWriter;
import io.StringReader;
import api.RedditApiClient;

import java.net.http.HttpResponse;

public class AccountLogin {
    private final StringReader stringReader;
    private final SessionService sessionService;
    private final OutputWriter output;
    private final Gson gson;

    public AccountLogin(StringReader stringReader, SessionService sessionService, OutputWriter output) {
        this.stringReader = stringReader;
        this.sessionService = sessionService;
        this.output = output;
        this.gson = RedditApiClient.gson();
    }

    public void Login() {
        String username = stringReader.readString("Enter username: ");
        String password = stringReader.readString("Enter password: ");

        try {
            HttpResponse<String> response = RedditApiClient.login(username, password);
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                AccountApiDtos.AccountResponse account =
                        gson.fromJson(response.body(), AccountApiDtos.AccountResponse.class);
                sessionService.login(account.username, account.email, account.id);
                output.write("Login Successful. Logged in as: " + account.username);
            } else if (RedditApiClient.isClientError(status)) {
                output.write(response.body());
            } else {
                output.write("Login failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
        }
    }
}
