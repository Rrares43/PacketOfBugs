package com.example.springreddit.controller;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerAccount(@Valid @RequestBody AccountDto.RegistrationRequest request) {
        try {
            Account savedAccount = accountService.registerAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedAccount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody AccountDto.LoginRequest request) {
        try {
            Account loggedInAccount = accountService.authenticateUser(request);
            return ResponseEntity.ok(toResponse(loggedInAccount));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getAccount(@PathVariable String username) {
        try {
            return ResponseEntity.ok(toResponse(accountService.getByUsername(username)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@Valid @RequestBody AccountDto.ChangePasswordRequest request) {
        try {
            accountService.changePassword(request);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteAccount(@PathVariable String username) {
        try {
            accountService.deleteAccount(username);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private AccountDto.AccountResponse toResponse(Account account) {
        AccountDto.AccountResponse response = new AccountDto.AccountResponse();
        response.setId(account.getId());
        response.setUsername(account.getUsername());
        response.setEmail(account.getEmail());
        return response;
    }
}
