package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account registerAccount(AccountDto.RegistrationRequest request) {
        log.debug("Attempting to register account with username: {}", request.getUsername());
        validateRegistrationRequest(request);
        if (accountRepository.existsByUsername(request.getUsername())) {
            log.warn("Registration failed: username already exists: {}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(request.getPassword());

        Account saved = accountRepository.save(newAccount);
        saved.setEmail(request.getEmail());
        log.info("Account registered successfully for username: {}", request.getUsername());
        return saved;
    }

    public Account authenticateUser(AccountDto.LoginRequest request) {
        if (request == null) {
            log.warn("Authentication attempt with null request");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            log.warn("Authentication attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            log.warn("Authentication attempt with blank password for username: {}", request.getUsername());
            throw new IllegalArgumentException("Password cannot be blank");
        }
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Authentication failed: user not found for username: {}", request.getUsername());
                    return new IllegalArgumentException("Invalid credentials");
                });

        if (!account.getPassword().equals(request.getPassword())) {
            log.warn("Authentication failed: invalid password for username: {}", request.getUsername());
            throw new IllegalArgumentException("Invalid credentials");
        }
        log.info("User authenticated successfully: {}", request.getUsername());
        return account;
    }

    public Account getByUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
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
            log.warn("Change password attempt with null request");
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            log.warn("Change password attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            log.warn("Change password attempt with blank email for username: {}", request.getUsername());
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            log.warn("Change password attempt with blank new password for username: {}", request.getUsername());
            throw new IllegalArgumentException("New password cannot be blank");
        }
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> {
                    log.warn("Change password failed: account not found for username: {}", request.getUsername());
                    return new IllegalArgumentException("Account not found");
                });

        if (!request.getEmail().equals(account.getEmail())) {
            log.warn("Change password failed: incorrect email for username: {}", request.getUsername());
            throw new IllegalArgumentException("Incorrect email address");
        }

        account.setPassword(request.getNewPassword());
        accountRepository.save(account);
        log.info("Password changed successfully for username: {}", request.getUsername());
    }

    @Transactional
    public void deleteAccount(String username) {
        if (username == null || username.isBlank()) {
            log.warn("Delete account attempt with blank username");
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (!accountRepository.existsByUsername(username)) {
            log.warn("Delete account failed: account not found for username: {}", username);
            throw new IllegalArgumentException("Account not found");
        }
        accountRepository.deleteByUsername(username);
        log.info("Account deleted successfully for username: {}", username);
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
