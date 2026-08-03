package account.verification;

public class PasswordVerification implements AccountVerifier {
    public PasswordVerification(){}

    public static boolean verify(String password){
        if(password == null){
            System.out.println("Password cannot be null");
            return false;
        }
        
        boolean correctLength = false;
        boolean hasLetter = false;
        boolean hasNumber = false;
        boolean hasUppercase = false;
        boolean hasLowercase = false;
        boolean hasSpecial = false;
        boolean hasWhitespace = false;

        if(password.length() >= 8 && password.length() < 40){
            correctLength = true;
        }
        for(int i = 0; i < password.length();i++){
            if(Character.isDigit(password.charAt(i))){
                hasNumber = true;
            }
            if(Character.isAlphabetic(password.charAt(i))){
                hasLetter = true;
            }
            if(Character.isUpperCase(password.charAt(i))){
                hasUppercase = true;
            }
            if(Character.isLowerCase(password.charAt(i))){
                hasLowercase = true;
            }
            if(!Character.isLetterOrDigit(password.charAt(i)) && !Character.isWhitespace(password.charAt(i))){
                hasSpecial = true;
            }
            if(Character.isWhitespace(password.charAt(i))){
                hasWhitespace = true;
            }
        }
        if(hasWhitespace){
            System.out.println("Password cannot contain whitespace");
            return false;
        }
        else if(correctLength && hasLetter && hasNumber && hasUppercase && hasLowercase && hasSpecial){
            return true;
        }
        else{
            System.out.println("Password does not meet the requirements");
            return false;
        }
    }
}
