package account.command;

import account.operations.AccountCreator;

public class CreateAccountCommand implements AccountCommand {
    private final AccountCreator AccountCreator;

    public CreateAccountCommand(AccountCreator AccountCreator) {
        this.AccountCreator = AccountCreator;
    }

    @Override
    public void execute(){
        AccountCreator.createAccount();
    }
}
