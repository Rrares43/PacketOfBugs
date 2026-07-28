package account.command;

import account.SessionService;
import account.dto.AccountApiDtos;
import account.operations.AccountInfo;
import com.google.gson.Gson;
import persistence.RedditApiClient;

import java.net.http.HttpResponse;

public class CheckCurrentUserCommand implements AccountCommand {
    private final SessionService session;
    private final Gson gson = RedditApiClient.gson();

    public CheckCurrentUserCommand(AccountInfo accountInfo, SessionService session) {
        this.session = session;
    }

    @Override
    public void execute() {
        if (!session.isLoggedIn()) {
            System.out.println("Logged in as GUEST");
            return;
        }

        String currentUser = session.getCurrentUsername();
        try {
            HttpResponse<String> response = RedditApiClient.getAccountRaw(currentUser);
            int status = response.statusCode();

            if (RedditApiClient.isSuccess(status)) {
                AccountApiDtos.AccountResponse account =
                        gson.fromJson(response.body(), AccountApiDtos.AccountResponse.class);
                String email = account.email != null ? account.email : session.getCurrentEmail();
                AccountInfo.checkUser(account.username, email);
            } else if (RedditApiClient.isClientError(status)) {
                AccountInfo.checkUser(currentUser, session.getCurrentEmail());
            } else {
                System.out.println("Could not fetch account. Status " + status + ": " + response.body());
            }
        } catch (Exception e) {
            AccountInfo.checkUser(currentUser, session.getCurrentEmail());
        }
    }
}
