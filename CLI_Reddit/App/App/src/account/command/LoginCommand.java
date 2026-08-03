package account.command;

import account.operations.AccountLogin;

public class LoginCommand implements AccountCommand {
    private final AccountLogin accountLogin;

    public LoginCommand(AccountLogin accountLogin) {
        this.accountLogin = accountLogin;
    }

    @Override
    public void execute(){
        accountLogin.Login();
    }
}
