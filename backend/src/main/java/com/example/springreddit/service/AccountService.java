package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account registerAccount(AccountDto.RegistrationRequest request) {
        com.example.springreddit.logging.CustomLogger.getInstance().info("Attempting to register account with username: {}", request.getUsername());
        validateRegistrationRequest(request);
        if (accountRepository.existsByUsername(request.getUsername())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Registration failed: username already exists: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        // Encode password before saving
        newAccount.setPassword(passwordEncoder.encode(request.getPassword()));

        Account saved = accountRepository.save(newAccount);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Account registered successfully for username: {}", request.getUsername());
        return saved;
    }

    public Account authenticateUser(AccountDto.LoginRequest request) {
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Authentication attempt with null request");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Authentication attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Authentication attempt with blank password for username: {}", request.getUsername());
            throw new IllegalArgumentException("Password cannot be blank");
        }
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Authentication failed: user not found for username: {}", request.getUsername());
                    return new IllegalArgumentException("Invalid credentials");
                });

        // Use passwordEncoder to verify password
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Authentication failed: invalid password for username: {}", request.getUsername());
            throw new IllegalArgumentException("Invalid credentials");
        }
        com.example.springreddit.logging.CustomLogger.getInstance().info("User authenticated successfully: {}", request.getUsername());
        return account;
    }

    public Account getByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public boolean isUsernameAvailable(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return !accountRepository.existsByUsername(username);
    }

    public Account getById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Account ID cannot be null");
        }
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    @Transactional
    public void changePassword(AccountDto.ChangePasswordRequest request) {
        if (request == null) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Change password attempt with null request");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Change password attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Change password attempt with blank email for username: {}", request.getUsername());
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Change password attempt with blank new password for username: {}", request.getUsername());
            throw new IllegalArgumentException("New password cannot be blank");
        }
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn("Change password failed: account not found for username: {}", request.getUsername());
                    return new IllegalArgumentException("Account not found");
                });

        if (!request.getEmail().equals(account.getEmail())) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Change password failed: incorrect email for username: {}", request.getUsername());
            throw new IllegalArgumentException("Incorrect email address");
        }

        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Password changed successfully for username: {}", request.getUsername());
    }

    @Transactional
    public void deleteAccount(String username) {
        if (username == null || username.isBlank()) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete account attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (!accountRepository.existsByUsername(username)) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete account failed: account not found for username: {}", username);
            throw new IllegalArgumentException("Account not found");
        }
        accountRepository.deleteByUsername(username);
        com.example.springreddit.logging.CustomLogger.getInstance().info("Account deleted successfully for username: {}", username);
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
