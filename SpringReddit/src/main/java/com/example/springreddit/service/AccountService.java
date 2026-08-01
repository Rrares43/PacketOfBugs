package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account registerAccount(AccountDto.RegistrationRequest request) {
        validateRegistrationRequest(request);
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(request.getPassword());

        Account saved = accountRepository.save(newAccount);
        saved.setEmail(request.getEmail());
        return saved;
    }

    public Account authenticateUser(AccountDto.LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!account.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
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
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!request.getEmail().equals(account.getEmail())) {
            throw new IllegalArgumentException("Incorrect email address");
        }

        account.setPassword(request.getNewPassword());
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (!accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Account not found");
        }
        accountRepository.deleteByUsername(username);
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
