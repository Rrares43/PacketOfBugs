package account.command;

import account.operations.AccountInfo;

public class CheckCurrentUserCommand implements AccountCommand {
    private final AccountInfo accountInfo;

    public CheckCurrentUserCommand(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    @Override
    public void execute() {
        accountInfo.checkCurrentUser();
    }
}