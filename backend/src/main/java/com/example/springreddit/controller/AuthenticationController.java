package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.service.AccountService;
import com.example.springreddit.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountService accountService;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

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
            LOGGER.info(
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

            LOGGER.info(
                    "Login successful for username: {}", loginRequest.getUsername());

            return ResponseEntity.ok(ApiResponse.success(authResponse));
        } catch (AuthenticationException e) {
            LOGGER.warn(
                    "Login failed for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(401).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/auth/login"));
        } catch (Exception e) {
            LOGGER.error(
                    "Login error for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            return ResponseEntity.status(500).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/auth/login"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto.UserProfile>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                LOGGER.warn(
                        "GET /auth/me request failed: user not authenticated");
                return ResponseEntity.status(401).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/auth/me"));
            }

            String username = authentication.getName();
            LOGGER.info(
                    "GET /auth/me request received for username: {}", username);

            AccountDto.UserProfile userProfile = accountService.getCurrentUserProfile(username);

            LOGGER.info(
                    "GET /auth/me request successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success(userProfile));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "GET /auth/me request failed: {}", e.getMessage());
            return ResponseEntity.status(401).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/auth/me"));
        } catch (Exception e) {
            LOGGER.error(
                    "GET /auth/me request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/auth/me"));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto.UserProfile>> updateCurrentUser(
            @Valid @RequestBody AccountDto.UpdateUserProfileRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                LOGGER.warn(
                        "PUT /auth/me request failed: user not authenticated");
                return ResponseEntity.status(401).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/auth/me"));
            }

            String username = authentication.getName();
            LOGGER.info(
                    "PUT /auth/me request received for username: {} with request: {}", username, request);

            AccountDto.UserProfile userProfile = accountService.updateUserProfile(username, request);

            LOGGER.info(
                    "PUT /auth/me request successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success(userProfile));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "PUT /auth/me request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/auth/me"));
        } catch (Exception e) {
            LOGGER.error(
                    "PUT /auth/me request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/auth/me"));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody AccountDto.UpdatePasswordRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                LOGGER.warn(
                        "PUT /auth/me/password request failed: user not authenticated");
                return ResponseEntity.status(401).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/auth/me/password"));
            }

            String username = authentication.getName();
            LOGGER.info(
                    "PUT /auth/me/password request received for username: {}", username);

            accountService.changePassword(username, request);

            LOGGER.info(
                    "PUT /auth/me/password request successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "PUT /auth/me/password request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/auth/me/password"));
        } catch (Exception e) {
            LOGGER.error(
                    "PUT /auth/me/password request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/auth/me/password"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AccountDto.AuthResponse>> register(
            @Valid @RequestBody AccountDto.RegistrationRequest registrationRequest) {
        try {
            LOGGER.info(
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

            LOGGER.info(
                    "Registration successful for username: {}", account.getUsername());

            return ResponseEntity.ok(ApiResponse.success(authResponse));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/auth/register"));
        } catch (Exception e) {
            LOGGER.error(
                    "Registration failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(500).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/auth/register"));
        }
    }
}
