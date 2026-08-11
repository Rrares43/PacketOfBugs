package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public Account registerAccount(AccountDto.RegistrationRequest request) {
        LOGGER.info("Attempting to register account with username: {}", request.getUsername());
        validateRegistrationRequest(request);
        if (accountRepository.existsByUsername(request.getUsername())) {
            LOGGER.warn("Registration failed: username already exists: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        if (accountRepository.existsByEmail(request.getEmail())) {
            LOGGER.warn("Registration failed: email already exists: {}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        // Encode password before saving
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        Account saved = accountRepository.save(newAccount);
        LOGGER.info("Account registered successfully for username: {}", request.getUsername());
        return saved;
    }

    public Account getByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public AccountDto.UserProfile getCurrentUserProfile(String username) {
        if (username == null || username.isBlank()) {
            LOGGER.warn("Get user profile failed: username is blank");
            throw new IllegalArgumentException("Username cannot be blank");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Get user profile failed: account not found for username: {}", username);
                    return new IllegalArgumentException("Account not found");
                });

        AccountDto.UserProfile userProfile = new AccountDto.UserProfile();
        userProfile.setUsername(account.getUsername());
        userProfile.setEmail(account.getEmail());
        userProfile.setDisplayName(account.getDisplayName());
        userProfile.setAvatarUrl(account.getAvatarUrl());

        LOGGER.info(
                "User profile retrieved successfully for username: {}", username);
        return userProfile;
    }

    @Transactional
    public AccountDto.UserProfile updateUserProfile(String username, AccountDto.UpdateUserProfileRequest request) {
        if (username == null || username.isBlank()) {
            LOGGER.warn("Update user profile failed: username is blank");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request == null) {
            LOGGER.warn("Update user profile failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Update user profile failed: account not found for username: {}", username);
                    return new IllegalArgumentException("Account not found");
                });

        boolean updated = false;
        if (request.getDisplayName() != null) {
            account.setDisplayName(request.getDisplayName());
            updated = true;
        }
        if (request.getAvatarUrl() != null) {
            account.setAvatarUrl(request.getAvatarUrl());
            updated = true;
        }

        if (updated) {
            accountRepository.save(account);
            LOGGER.info(
                    "User profile updated successfully for username: {}", username);
        } else {
            LOGGER.info(
                    "User profile update: no changes for username: {}", username);
        }

        AccountDto.UserProfile userProfile = new AccountDto.UserProfile();
        userProfile.setUsername(account.getUsername());
        userProfile.setEmail(account.getEmail());
        userProfile.setDisplayName(account.getDisplayName());
        userProfile.setAvatarUrl(account.getAvatarUrl());

        return userProfile;
    }

    @Transactional
    public void changePassword(String username, AccountDto.UpdatePasswordRequest request) {
        if (username == null || username.isBlank()) {
            LOGGER.warn("Change password failed: username is blank");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request == null) {
            LOGGER.warn("Change password failed: request is null");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            LOGGER.warn("Change password failed: current password is blank");
            throw new IllegalArgumentException("Current password cannot be blank");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            LOGGER.warn("Change password failed: new password is blank");
            throw new IllegalArgumentException("New password cannot be blank");
        }

        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    LOGGER.warn(
                            "Change password failed: account not found for username: {}", username);
                    return new IllegalArgumentException("Account not found");
                });

        if (!passwordEncoder.matches(request.getCurrentPassword(), account.getPassword())) {
            LOGGER.warn(
                    "Change password failed: incorrect current password for username: {}", username);
            throw new IllegalArgumentException("Current password is incorrect");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        LOGGER.info(
                "Password changed successfully for username: {}", username);
    }

    @Transactional
    public void deleteAccount(String username, String password) {
        if (username == null || username.isBlank()) {
            LOGGER.warn("Delete account attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (!accountRepository.existsByUsername(username)) {
            LOGGER.warn("Delete account failed: account not found for username: {}", username);
            throw new IllegalArgumentException("Account not found");
        }

        Optional<Account> account = accountRepository.findByUsername(username);

        if (!passwordEncoder.matches(password, account.get().getPassword())) {
            LOGGER.warn("Delete account failed: invalid password for username: {}", username);
            throw new IllegalArgumentException("Invalid password");
        }


        accountRepository.deleteByUsername(username);
        LOGGER.info("Account deleted successfully for username: {}", username);
    }

    private void validateRegistrationRequest(AccountDto.RegistrationRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getUsername().length() > 50) {
            throw new IllegalArgumentException("Username must not exceed 50 characters");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (request.getEmail().length() > 100) {
            throw new IllegalArgumentException("Email must not exceed 100 characters");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if (request.getPassword().length() > 100) {
            throw new IllegalArgumentException("Password must not exceed 100 characters");
        }
    }
}
