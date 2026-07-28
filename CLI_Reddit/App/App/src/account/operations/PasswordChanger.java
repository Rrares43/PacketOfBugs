package account.operations;

import account.repository.AccountRepository;

import java.util.Scanner;

public class PasswordChanger {
    public void ChangePassword(){
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Email:");
        String email = sc.nextLine();
        if (AccountRepository.checkEmail(email)) {
            System.out.println("Enter New Password: ");
            String password = sc.nextLine();
            AccountRepository.changePassword(email, password);
        }
        else {
            System.out.println("Email not found");
        }
    }
}
