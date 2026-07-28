package account.operations;

import account.Account;
import account.SessionService;
import account.repository.AccountRepository;
import io.OutputWriter;
import io.StringReader;

public class AccountLogin {
    private final StringReader stringReader;
    private final SessionService sessionService;
    private final OutputWriter output;

    public AccountLogin(StringReader stringReader, SessionService sessionService, OutputWriter output) {
        this.stringReader = stringReader;
        this.sessionService = sessionService;
        this.output = output;
    }

    public void Login() {
        String username = stringReader.readString("Enter username: ");
        String password = stringReader.readString("Enter password: ");

        if (AccountRepository.loginAccount(new Account(username, "", password))) {
            sessionService.login(username);
            output.write("Login Successful. Logged in as: " + username);
        } else {
            output.write("Login Failed");
        }
    }
}
