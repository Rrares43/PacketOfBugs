package account.command;

import account.operations.AccountDeleter;

public class DeleteAccountCommand implements AccountCommand{
    private final AccountDeleter accountDeleter;

    public DeleteAccountCommand(AccountDeleter accountDeleter) {
        this.accountDeleter = accountDeleter;
    }

    @Override
    public void execute(){
        accountDeleter.DeleteAccount();
    }
}
