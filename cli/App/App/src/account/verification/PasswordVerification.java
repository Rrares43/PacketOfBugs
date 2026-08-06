package account.verification;

public class PasswordVerification  {

    public static boolean verify(String password){
        if(password == null){
            System.out.println("Password cannot be null");
            return false;
        }

        if(password.length() < 8){
            System.out.println("Password must be at least 8 characters");
            return false;
        }

        return true;
    }
}
