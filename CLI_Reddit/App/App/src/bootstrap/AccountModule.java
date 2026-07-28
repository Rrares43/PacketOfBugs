package bootstrap;

import account.command.*;
import account.operations.*;
import account.AccountMenu;
import account.SessionService;
import io.OutputWriter;
import io.StringReader;

final class AccountModule {
    private AccountModule() {
    }

    static AccountMenu create(StringReader stringReader, OutputWriter output, SessionService sessionService) {
        AccountMenu accountMenu = new AccountMenu(stringReader, output, sessionService);
        AccountCreator accountCreator = new AccountCreator(stringReader, output);
        AccountLogin accountLogin = new AccountLogin(stringReader, sessionService, output);
        PasswordChanger passwordChanger = new PasswordChanger();
        AccountInfo accountInfo = new AccountInfo();
        AccountLogout accountLogout = new AccountLogout(sessionService);
        AccountDeleter accountDeleter = new AccountDeleter(stringReader, sessionService);

        accountMenu.registerCommand("1", new CreateAccountCommand(accountCreator));
        accountMenu.registerCommand("2", new LoginCommand(accountLogin));
        accountMenu.registerCommand("3", new ChangePasswordCommand(passwordChanger));
        accountMenu.registerCommand("4", new CheckCurrentUserCommand(accountInfo, sessionService));
        accountMenu.registerCommand("5", new LogoutCommand(accountLogout));
        accountMenu.registerCommand("6", new DeleteAccountCommand(accountDeleter));
        return accountMenu;
    }
}
