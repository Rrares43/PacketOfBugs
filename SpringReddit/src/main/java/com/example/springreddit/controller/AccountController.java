package com.example.springreddit.controller;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/register")
    public ResponseEntity<?> registerAccount(@RequestBody AccountDto.RegistrationRequest request){
        try{
            Account savedAccount = accountService.registerAccount(request);

            AccountDto.AccountResponse response = new AccountDto.AccountResponse();
            response.setId(savedAccount.getId());
            response.setUsername(savedAccount.getUsername());
            response.setEmail(savedAccount.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AccountDto.LoginRequest request){
        try{
            Account loggedInAccount = accountService.authenticateUser(request);

            AccountDto.AccountResponse response = new AccountDto.AccountResponse();
            response.setId(loggedInAccount.getId());
            response.setUsername(loggedInAccount.getUsername());
            response.setEmail(loggedInAccount.getEmail());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
        catch(IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody AccountDto.ChangePasswordRequest request){
        try{
            accountService.changePassword(request);
            return ResponseEntity.status(HttpStatus.OK).body("Password changed successfully");
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
