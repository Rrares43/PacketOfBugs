package account.command;

import account.operations.AccountCreator;

public class CreateAccountCommand implements AccountCommand {
    private final AccountCreator accountCreator;

    public CreateAccountCommand(AccountCreator accountCreator) {
        this.accountCreator = accountCreator;
    }

    @Override
    public void execute(){
        accountCreator.createAccount();
    }
}
