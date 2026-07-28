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
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(request.getPassword());

        Account saved = accountRepository.save(newAccount);
        // email is @Transient — restore from request for the response payload
        saved.setEmail(request.getEmail());
        return saved;
    }

    public Account authenticateUser(AccountDto.LoginRequest request) {
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!account.getPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }
        return account;
    }

    public Account getByUsername(String username) {
        return accountRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public Account getById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    @Transactional
    public void changePassword(AccountDto.ChangePasswordRequest request) {
        Account account = accountRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        if (!request.getOldPassword().equals(account.getPassword())) {
            throw new IllegalArgumentException("Old password does not match");
        }

        account.setPassword(request.getNewPassword());
        accountRepository.save(account);
    }

    @Transactional
    public void deleteAccount(String username) {
        if (!accountRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Account not found");
        }
        accountRepository.deleteByUsername(username);
    }
}
