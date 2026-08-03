package menu;

import account.AccountMenu;

public class AccountCommand implements MenuCommand {
    private final AccountMenu accountMenu;

    public AccountCommand(AccountMenu accountMenu) {
        this.accountMenu = accountMenu;
    }
    @Override
    public void execute(){
        accountMenu.startAccountMenu();
    }
}

