package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.service.AccountService;
import com.example.springreddit.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountService accountService;

    public AuthenticationController(AuthenticationManager authenticationManager,
                                    JwtTokenProvider jwtTokenProvider,
                                    AccountService accountService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccountDto.AuthResponse>> login(
            @Valid @RequestBody AccountDto.LoginRequest loginRequest) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "Login attempt for username: {}", loginRequest.getUsername());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwt = jwtTokenProvider.generateToken(userDetails);

            Account account = accountService.getByUsername(loginRequest.getUsername());

            AccountDto.UserInfo userInfo = new AccountDto.UserInfo();
            userInfo.setUsername(account.getUsername());
            userInfo.setEmail(account.getEmail());

            AccountDto.AuthResponse authResponse = new AccountDto.AuthResponse();
            authResponse.setAccessToken(jwt);
            authResponse.setUser(userInfo);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "Login successful for username: {}", loginRequest.getUsername());

            return ResponseEntity.ok(ApiResponse.success(authResponse));
        } catch (AuthenticationException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "Login failed for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(
                    new ApiResponse<>(false, null));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "Login error for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(500).body(
                    new ApiResponse<>(false, null));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto.UserProfile>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "GET /auth/me request failed: user not authenticated");
                return ResponseEntity.status(401).body(
                        new ApiResponse<>(false, null));
            }

            String username = authentication.getName();
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /auth/me request received for username: {}", username);

            AccountDto.UserProfile userProfile = accountService.getCurrentUserProfile(username);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /auth/me request successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success(userProfile));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "GET /auth/me request failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                    new ApiResponse<>(false, null));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "GET /auth/me request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                    new ApiResponse<>(false, null));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AccountDto.AuthResponse>> register(
            @Valid @RequestBody AccountDto.RegistrationRequest registrationRequest) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "Registration attempt for username: {}", registrationRequest.getUsername());

            Account account = accountService.registerAccount(registrationRequest);

            UserDetails userDetails = new CustomUserDetails(account);
            String jwt = jwtTokenProvider.generateToken(userDetails);

            AccountDto.UserInfo userInfo = new AccountDto.UserInfo();
            userInfo.setUsername(account.getUsername());
            userInfo.setEmail(account.getEmail());

            AccountDto.AuthResponse authResponse = new AccountDto.AuthResponse();
            authResponse.setAccessToken(jwt);
            authResponse.setUser(userInfo);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "Registration successful for username: {}", account.getUsername());

            return ResponseEntity.ok(ApiResponse.success(authResponse));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new ApiResponse<>(false, null));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "Registration failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                    new ApiResponse<>(false, null));
        }
    }
}
