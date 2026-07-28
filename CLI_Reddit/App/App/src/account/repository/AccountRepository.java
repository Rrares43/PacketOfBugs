package account.repository;

import account.Account;
import account.verification.EmailVerification;
import account.verification.PasswordVerification;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import logger.LogLevel;
import logger.Logger;
import persistence.DataPaths;
import persistence.DatabaseSync;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON-primary account store under {@code CLI_Reddit/App/data/accounts.json},
 * with dual-write to the Spring API via {@link DatabaseSync}.
 */
public class AccountRepository {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static Path file() {
        return DataPaths.resolveDataFile("accounts.json");
    }

    public static List<Account> loadAccounts() {
        Path path = file();
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }
        try (Reader reader = Files.newBufferedReader(path)) {
            Type listType = new TypeToken<ArrayList<Account>>(){}.getType();
            List<Account> accounts = gson.fromJson(reader, listType);
            return accounts != null ? accounts : new ArrayList<>();
        } catch (IOException e) {
            System.out.println("Error");
            Logger.getInstance().log(LogLevel.ERROR, "Error reading accounts.json");
            return new ArrayList<>();
        }
    }

    private static void writeAccounts(List<Account> accounts) {
        Path path = file();
        try {
            DataPaths.ensureParent(path);
            try (Writer fileWriter = Files.newBufferedWriter(path)) {
                gson.toJson(accounts, fileWriter);
            }
        } catch (IOException e) {
            System.out.println("Error: Could not write to file");
            Logger.getInstance().log(LogLevel.ERROR, "Could not write accounts.json: " + e.getMessage());
            return;
        }
        DatabaseSync.syncAccounts(accounts);
    }

    /** Persist without remote sync (used after HTTP ops that already updated Spring). */
    private static void writeAccountsLocalOnly(List<Account> accounts) {
        Path path = file();
        try {
            DataPaths.ensureParent(path);
            try (Writer fileWriter = Files.newBufferedWriter(path)) {
                gson.toJson(accounts, fileWriter);
            }
        } catch (IOException e) {
            System.out.println("Error: Could not write to file");
            Logger.getInstance().log(LogLevel.ERROR, "Could not write accounts.json: " + e.getMessage());
        }
    }

    public static void saveAccount(Account account) {
        Logger logger = Logger.getInstance();
        if (account.getUsername().isBlank() || account.getEmail().isBlank() || account.getPassword().isBlank()) {
            System.out.println("Please fill in all fields");
            return;
        }
        if (PasswordVerification.verify(account.getPassword())
                && !usernameExists(account.getUsername())
                && EmailVerification.verify(account.getEmail())) {
            List<Account> accounts = loadAccounts();
            accounts.add(account);
            writeAccounts(accounts);
            System.out.println("Account saved successfully!");
            logger.log(LogLevel.INFO, "Account saved successfully!");
        } else if (!EmailVerification.verify(account.getEmail())) {
            System.out.println("Email condition not respected");
            logger.log(LogLevel.WARNING, "Email condition not respected");
        } else if (!PasswordVerification.verify(account.getPassword())) {
            System.out.println("Password condition not respected");
            logger.log(LogLevel.WARNING, "Password condition not respected");
        } else {
            System.out.println("Account already exists");
            logger.log(LogLevel.ERROR, "Account already exists");
        }
    }

    /** Upsert into local JSON after a successful Spring register (no second remote create). */
    public static void upsertLocalAccount(String username, String email, String password) {
        List<Account> accounts = loadAccounts();
        for (Account acc : accounts) {
            if (acc.getUsername().equals(username)) {
                acc.setEmail(email);
                acc.setPassword(password);
                writeAccountsLocalOnly(accounts);
                return;
            }
        }
        accounts.add(new Account(username, email, password));
        writeAccountsLocalOnly(accounts);
    }

    public static boolean loginAccount(Account account) {
        Logger logger = Logger.getInstance();
        List<Account> accounts = loadAccounts();
        for (Account acc : accounts) {
            if (acc.getUsername().equals(account.getUsername())
                    && acc.getPassword().equals(account.getPassword())) {
                logger.log(LogLevel.INFO, acc.getUsername() + " has logged in.");
                return true;
            }
        }
        return false;
    }

    public static boolean usernameExists(String username) {
        return getAccountByUsername(username) != null;
    }

    public static void changePassword(String email, String newPassword) {
        Logger logger = Logger.getInstance();
        List<Account> accounts = loadAccounts();
        boolean found = false;
        for (Account acc : accounts) {
            if (acc.getEmail().equals(email)) {
                acc.setPassword(newPassword);
                found = true;
                break;
            }
        }
        if (!found) {
            // Also try by matching after HTTP password change via username helpers
            System.out.println("Account not found");
            logger.log(LogLevel.ERROR, "Account not found");
            return;
        }
        writeAccounts(accounts);
        System.out.println("Password changed successfully!");
        logger.log(LogLevel.INFO, "Password changed successfully!");
    }

    public static void updateLocalPassword(String username, String newPassword) {
        List<Account> accounts = loadAccounts();
        for (Account acc : accounts) {
            if (acc.getUsername().equals(username)) {
                acc.setPassword(newPassword);
                writeAccountsLocalOnly(accounts);
                return;
            }
        }
    }

    public static boolean checkEmail(String email) {
        for (Account acc : loadAccounts()) {
            if (acc.getEmail().equals(email)) {
                return true;
            }
        }
        return false;
    }

    public static Account getAccountByUsername(String username) {
        for (Account acc : loadAccounts()) {
            if (acc.getUsername().equals(username)) {
                return acc;
            }
        }
        return null;
    }

    public static void deleteAccount(String currentUsername) {
        List<Account> accounts = loadAccounts();
        boolean removed = accounts.removeIf(account -> account.getUsername().equals(currentUsername));
        if (removed) {
            writeAccounts(accounts);
            System.out.println("Account deleted successfully!");
            Logger.getInstance().log(LogLevel.INFO, "Account deleted successfully!");
        } else {
            System.out.println("Account not found");
            Logger.getInstance().log(LogLevel.ERROR, "Account not found");
        }
    }

    public static void deleteLocalAccount(String username) {
        List<Account> accounts = loadAccounts();
        boolean removed = accounts.removeIf(account -> account.getUsername().equals(username));
        if (removed) {
            writeAccountsLocalOnly(accounts);
        }
    }
}
