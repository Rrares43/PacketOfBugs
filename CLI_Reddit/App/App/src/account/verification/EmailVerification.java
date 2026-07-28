package account.verification;

public class EmailVerification implements AccountVerifier {
    public EmailVerification(){}
    public static boolean verify(String email) {
        if(email == null || email.isBlank()){
            return false;
        }
        int index1 = email.indexOf("@");
        int index2 = email.indexOf(".");
        if(index1 == -1 || index2 == -1 || index2 - index1 < 2){
            System.out.println("Email is not valid");
            return false;
        }
        return true;
    }
}
