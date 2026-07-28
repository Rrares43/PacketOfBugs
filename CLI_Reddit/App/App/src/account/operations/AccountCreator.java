package account.operations;

import account.Account;
import account.repository.AccountRepository;
import account.verification.EmailVerification;
import account.verification.PasswordVerification;
import io.OutputWriter;
import io.StringReader;


public class AccountCreator {
    private final StringReader stringReader;
    private final OutputWriter output;

    public AccountCreator(StringReader stringReader, OutputWriter output) {
        this.stringReader = stringReader;
        this.output = output;
    }

    public void createAccount(){
            String answer = stringReader.readString("Create an account? (y/n)");
            if(!answer.equalsIgnoreCase("y")) {
                output.write("Account creation cancelled.");
                return;
            }

            String username = stringReader.readString("Enter username: (or 0 to cancel)");
            if(username.equals("0")) {
                return;
            }

            String email;
            while(true) {
                email = stringReader.readString("Enter email: (or 0 to cancel)");
                if(email.equals("0")) {
                    return;
                }
                if(EmailVerification.verify(email)) {
                    break;
                }
                else {
                    output.write("Invalid email format!");
                }
            }

        String password;
        while (true) {
            output.write("Password must be at least 8 characters long and contain at least one number, one uppercase letter, one lowercase letter, and one special character.");
            password = stringReader.readString("Enter Password (or 0 to cancel): ");
            if (password.equals("0")) return;

            if (PasswordVerification.verify(password)) {
                break;
            } else {
                output.write("Weak password. Please try again.");
            }
        }

            Account account = new Account(username, email, password);
            AccountRepository.saveAccount(account);
        }
    }
