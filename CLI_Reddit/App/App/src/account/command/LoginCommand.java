package account.command;

import account.operations.AccountLogin;

public class LoginCommand implements AccountCommand {
    private final AccountLogin AccountLogin;

    public LoginCommand(AccountLogin AccountLogin) {
        this.AccountLogin = AccountLogin;
    }

    @Override
    public void execute(){
        AccountLogin.Login();
    }
}
