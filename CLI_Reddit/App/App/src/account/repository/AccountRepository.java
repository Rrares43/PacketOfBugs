package account.repository;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import account.Account;
import account.verification.EmailVerification;
import account.verification.PasswordVerification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import logger.Logger;
import logger.LogLevel;
import persistence.DatabaseSync;

public class AccountRepository {
    private static final String FILE_NAME = "App/data/accounts.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static List<Account> loadAccounts() {
        File file = new File(FILE_NAME);
        if(!file.exists()){
            return new ArrayList<>();
        }
        try(FileReader reader = new FileReader(FILE_NAME)){
            Type listType = new TypeToken<ArrayList<Account>>(){}.getType();
            List<Account> accounts = gson.fromJson(reader, listType);
            return accounts != null ? accounts : new ArrayList<>();
        }
        catch(IOException e){
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR, "Error");
            return new ArrayList<>();
        }
    }

    private static void writeAccounts(List<Account> accounts) {
        try(FileWriter fileWriter = new FileWriter(FILE_NAME)){
            gson.toJson(accounts, fileWriter);
        }
        catch (IOException e){
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR, "Error");
            return;
        }
        DatabaseSync.syncAccounts(accounts);
    }

    public static void saveAccount(Account account) {
        Logger logger = Logger.getInstance();
        if(account.getUsername().isBlank() || account.getEmail().isBlank() || account.getPassword().isBlank()){
            System.out.println("Please fill in all fields");
        }
        else if (PasswordVerification.verify(account.getPassword()) && !loginAccount(account) && EmailVerification.verify(account.getEmail())) {
            List<Account> accounts = loadAccounts();
            accounts.add(account);
            writeAccounts(accounts);
            System.out.println("Account saved successfully!");
            logger.log(LogLevel.INFO, "Account saved successfully!");
        }
        else if(!EmailVerification.verify(account.getEmail())){
            System.out.println("Email condition not respected");
            logger.log(LogLevel.WARNING, "Email condition not respected");
        }
        else if(!PasswordVerification.verify(account.getPassword())){
            System.out.println("Password condition not respected");
            logger.log(LogLevel.WARNING, "Password condition not respected");
        }
        else{
            System.out.println("Account already exists");
            logger.log(LogLevel.ERROR, "Account already exists");
        }
    }

    // used for checking if the account already exists
    public static boolean loginAccount(Account account){
        Logger logger = Logger.getInstance();
        List<Account> accounts = loadAccounts();

        for(Account acc : accounts){
            if(acc.getUsername().equals(account.getUsername()) && acc.getPassword().equals(account.getPassword())){
                logger.log(LogLevel.INFO, acc.getUsername() + " has logged in.");
                return true;
            }
        }
        return false;
    }

    // used for changing the password
    public static void changePassword(String email, String newPassword){
        Logger logger = Logger.getInstance();
        List<Account> accounts = loadAccounts();
        boolean found = false;

        for(Account acc : accounts){
            if(acc.getEmail().equals(email)){
                acc.setPassword(newPassword);
                found = true;
                break;
            }
        }

        if(!found){
            System.out.println("Account not found");
            logger.log(LogLevel.ERROR, "Account not found");
            return;
        }

        writeAccounts(accounts);
        System.out.println("Password changed successfully!");
        logger.log(LogLevel.INFO, "Password changed successfully!");
    }

    // used for checking if the email already exists
    public static boolean checkEmail(String email){
        List<Account> accounts = loadAccounts();

        for(Account acc : accounts){
            if(acc.getEmail().equals(email)){
                return true;
            }
        }
        return false;
    }

    public static Account getAccountByUsername(String username){
        List<Account> accounts = loadAccounts();

        for(Account acc : accounts){
            if(acc.getUsername().equals(username)){
                return acc;
            }
        }
        return null;
    }

    public static void deleteAccount(String currentUsername) {
        List<Account> accounts = loadAccounts();
        boolean removed = accounts.removeIf(account -> account.getUsername().equals(currentUsername));
        if(removed) {
            writeAccounts(accounts);
            System.out.println("Account deleted successfully!");
            Logger.getInstance().log(LogLevel.INFO, "Account deleted successfully!");
        }
        else {
            System.out.println("Account not found");
            Logger.getInstance().log(LogLevel.ERROR, "Account not found");
        }
    }
}
