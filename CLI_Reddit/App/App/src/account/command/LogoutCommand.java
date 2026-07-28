package account.command;

import account.operations.AccountLogout;

public class LogoutCommand implements AccountCommand{
    private AccountLogout accountLogout;

    public LogoutCommand(AccountLogout accountLogout){
        this.accountLogout = accountLogout;
    }

    @Override
    public void execute(){
        accountLogout.Logout();
    }
}
