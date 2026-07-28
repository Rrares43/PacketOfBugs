package account.operations;

import account.SessionService;
import account.command.LogoutCommand;
import account.repository.AccountRepository;
import io.StringReader;

public class AccountDeleter {
    private final StringReader stringReader;
    private final SessionService sessionService;

    public AccountDeleter(StringReader stringReader, SessionService sessionService) {
        this.stringReader = stringReader;
        this.sessionService = sessionService;
    }

    public void DeleteAccount(){
        String choice = stringReader.readString("Are you sure you want to delete your account? (y/n) (this will automatically log you out)");
        if(choice.equals("y")){
            AccountRepository.deleteAccount(sessionService.getCurrentUsername());
            sessionService.logout();
        }
    }
}
