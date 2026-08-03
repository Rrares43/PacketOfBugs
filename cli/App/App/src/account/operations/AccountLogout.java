package account.operations;

import account.SessionService;

public class AccountLogout {
    private final SessionService service;

    public AccountLogout(SessionService service) {
        this.service = service;
    }

    public void Logout() {
        System.out.println("Logged out of account");
        service.logout();
    }
}
