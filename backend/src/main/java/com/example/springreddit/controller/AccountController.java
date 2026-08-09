package com.example.springreddit.controller;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.mapper.AccountMapper;
import com.example.springreddit.model.Account;
import com.example.springreddit.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/accounts", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @PostMapping(value = "/register", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> registerAccount(@Valid @RequestBody AccountDto.RegistrationRequest request) {
        LOGGER.info("Register request received for username: {}", request.getUsername());
        Account savedAccount = accountService.registerAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountMapper.toResponse(savedAccount));
    }

    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> login(@RequestBody AccountDto.LoginRequest request) {
        LOGGER.info("Login request received for username: {}", request.getUsername());
        Account loggedInAccount = accountService.authenticateUser(request);
        return ResponseEntity.ok(accountMapper.toResponse(loggedInAccount));
    }

    @GetMapping("/{username}")
    public ResponseEntity<?> getAccount(@PathVariable String username) {
        return ResponseEntity.ok(accountMapper.toResponse(accountService.getByUsername(username)));
    }

    @PutMapping(value = "/password", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> changePassword(@Valid @RequestBody AccountDto.ChangePasswordRequest request) {
        LOGGER.info("Change password request received for username: {}", request.getUsername());
        accountService.changePassword(request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<?> deleteAccount(@PathVariable String username) {
        LOGGER.info("Delete account request received for username: {}", username);
        accountService.deleteAccount(username);
        return ResponseEntity.ok("Account deleted successfully");
    }

    @GetMapping("/available/{username}")
    public ResponseEntity<?> checkUsernameAvailability(@PathVariable String username) {
        return ResponseEntity.ok(accountService.isUsernameAvailable(username));
    }
}
