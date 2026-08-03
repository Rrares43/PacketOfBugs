package account.command;

import account.operations.PasswordChanger;

public class ChangePasswordCommand implements AccountCommand {
    private final PasswordChanger passwordChanger;

    public ChangePasswordCommand(PasswordChanger passwordChanger) {
        this.passwordChanger = passwordChanger;
    }


    @Override
    public void execute(){
        passwordChanger.ChangePassword();
    }
}
