package account.command;

import account.Account;
import account.operations.AccountInfo;
import account.repository.AccountRepository;
import account.SessionService;

public class CheckCurrentUserCommand implements AccountCommand{
    private final AccountInfo accountInfo;
    private final SessionService session;

    public CheckCurrentUserCommand(AccountInfo accountInfo, SessionService session){
        this.accountInfo = accountInfo;
        this.session = session;
    }
    @Override
    public void execute(){
        if(!session.isLoggedIn()) {
            System.out.println("Logged in as GUEST");
        }

        String currentUser = session.getCurrentUsername();
        Account currentAccount = AccountRepository.getAccountByUsername(currentUser);
        if(currentAccount != null) {
            AccountInfo.checkUser(currentAccount);
        }
    }
}
