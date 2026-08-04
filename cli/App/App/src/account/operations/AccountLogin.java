package account.operations;

import account.SessionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
                try {
                    JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                    
                    if (!jsonResponse.has("accessToken")) {
                        output.write("Login failed: Invalid response format - missing accessToken");
                        return;
                    }
                    
                    String accessToken = jsonResponse.get("accessToken").getAsString();
                    String returnedUsername = jsonResponse.get("username").getAsString();
                    String email = jsonResponse.get("email").getAsString();
                    Long userId = jsonResponse.get("userId").getAsLong();
                    
                    sessionService.login(returnedUsername, email, userId, accessToken);
                    RedditApiClient.setJwtToken(accessToken);
                    output.write("Login Successful. Logged in as: " + returnedUsername);
                } catch (Exception e) {
                    output.write("Login failed: Invalid response format - " + e.getMessage());
                }
            } else if (RedditApiClient.isClientError(status)) {
                String errorMessage = response.body();
                if (errorMessage != null && !errorMessage.isEmpty()) {
                    output.write("Login failed: " + errorMessage);
                } else {
                    output.write("Login failed with status: " + status);
                }
            } else {
                output.write("Login failed. Server returned status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            output.write("Connection error: " + e.getMessage());
        }
    }
}
