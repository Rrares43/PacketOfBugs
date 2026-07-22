package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

    public Account registerAccount(AccountDto.RegistrationRequest request){
        if(accountRepository.existsByUsername(request.getUsername())){
            throw new IllegalArgumentException("Username already exists");
        }
        if(accountRepository.existsByEmail(request.getEmail())){
            throw new IllegalArgumentException("Email already exists");
        }

        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());
        newAccount.setEmail(request.getEmail());
        newAccount.setPassword(request.getPassword());

        return accountRepository.save(newAccount);
    }

    public Account authenticateUser(AccountDto.LoginRequest request){
        Optional<Account> accountOptional = accountRepository.findByUsername(request.getUsername());

        if(accountOptional.isPresent()){
            Account account = accountOptional.get();
            if(account.getPassword().equals(request.getPassword())){
                return account;
            }
        }
        throw new IllegalArgumentException("Invalid credentials");
    }
}
