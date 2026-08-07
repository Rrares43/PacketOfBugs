package com.example.springreddit.controller;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
public class AccountController {

    @Autowired
    private AccountService accountService;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerAccount(@Valid @RequestBody AccountDto.RegistrationRequest request) {
        try {
            LOGGER.info("Register request received for username: {}", request.getUsername());
            Account savedAccount = accountService.registerAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedAccount));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Registration failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody AccountDto.LoginRequest request) {
        try {
            LOGGER.info("Login request received for username: {}", request.getUsername());
            Account loggedInAccount = accountService.authenticateUser(request);
            return ResponseEntity.ok(toResponse(loggedInAccount));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Login failed: {}", e.getMessage());
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
            LOGGER.info("Change password request received for username: {}", request.getUsername());
            accountService.changePassword(request);
            return ResponseEntity.ok("Password changed successfully");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Change password failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteAccount(@PathVariable String username) {
        try {
            LOGGER.info("Delete account request received for username: {}", username);
            accountService.deleteAccount(username);
            return ResponseEntity.ok("Account deleted successfully");
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Delete account failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/available/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        return ResponseEntity.ok(accountService.isUsernameAvailable(username));
    }

    private AccountDto.AccountResponse toResponse(Account account) {
        AccountDto.AccountResponse response = new AccountDto.AccountResponse();
        response.setId(account.getId());
        response.setUsername(account.getUsername());
        response.setEmail(account.getEmail());
        return response;
    }
}
