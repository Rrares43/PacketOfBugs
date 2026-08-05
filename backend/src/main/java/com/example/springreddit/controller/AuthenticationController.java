package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.dto.ErrorResponse;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.service.AccountService;
import com.example.springreddit.shared.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
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

    private ErrorResponse buildErrorResponse(String message, String code, HttpServletRequest request) {
        return ErrorResponse.builder()
                .success(false)
                .error(ErrorResponse.ErrorDetails.builder()
                        .code(code)
                        .message(message)
                        .details(new java.util.ArrayList<>())
                        .build())
                .timestamp(java.time.Instant.now().toString())
                .path(request.getRequestURI())
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody AccountDto.LoginRequest loginRequest, HttpServletRequest request) {
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
            ErrorResponse errorResponse = buildErrorResponse(
                    "Invalid username or password", 
                    "UNAUTHORIZED", 
                    request
            );
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "Login error for username: {} - {}", loginRequest.getUsername(), e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    "An unexpected error occurred during login", 
                    "INTERNAL_SERVER_ERROR", 
                    request
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "GET /auth/me request failed: user not authenticated");
                ErrorResponse errorResponse = buildErrorResponse(
                        "Authentication required", 
                        "UNAUTHORIZED", 
                        request
                );
                return ResponseEntity.status(401).body(errorResponse);
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
            ErrorResponse errorResponse = buildErrorResponse(
                    e.getMessage(), 
                    "UNAUTHORIZED", 
                    request
            );
            return ResponseEntity.status(401).body(errorResponse);
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "GET /auth/me request failed with unexpected error: {}", e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    "An unexpected error occurred", 
                    "INTERNAL_SERVER_ERROR", 
                    request
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(
            @Valid @RequestBody AccountDto.UpdateUserProfileRequest request, HttpServletRequest servletRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "PUT /auth/me request failed: user not authenticated");
                ErrorResponse errorResponse = buildErrorResponse(
                        "Authentication required", 
                        "UNAUTHORIZED", 
                        servletRequest
                );
                return ResponseEntity.status(401).body(errorResponse);
            }

            String username = authentication.getName();
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /auth/me request received for username: {} with request: {}", username, request);

            AccountDto.UserProfile userProfile = accountService.updateUserProfile(username, request);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /auth/me request successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success(userProfile));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /auth/me request failed: {}", e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    e.getMessage(), 
                    "BAD_REQUEST", 
                    servletRequest
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "PUT /auth/me request failed with unexpected error: {}", e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    "An unexpected error occurred", 
                    "INTERNAL_SERVER_ERROR", 
                    servletRequest
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody AccountDto.UpdatePasswordRequest request, HttpServletRequest servletRequest) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "PUT /auth/me/password request failed: user not authenticated");
                ErrorResponse errorResponse = buildErrorResponse(
                        "Authentication required", 
                        "UNAUTHORIZED", 
                        servletRequest
                );
                return ResponseEntity.status(401).body(errorResponse);
            }

            String username = authentication.getName();
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /auth/me/password request received for username: {}", username);

            accountService.changePassword(username, request);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /auth/me/password request successful for username: {}", username);
            return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /auth/me/password request failed: {}", e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    e.getMessage(), 
                    "BAD_REQUEST", 
                    servletRequest
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "PUT /auth/me/password request failed with unexpected error: {}", e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    "An unexpected error occurred", 
                    "INTERNAL_SERVER_ERROR", 
                    servletRequest
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody AccountDto.RegistrationRequest registrationRequest, HttpServletRequest request) {
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
            ErrorResponse errorResponse = buildErrorResponse(
                    e.getMessage(), 
                    "BAD_REQUEST", 
                    request
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "Registration failed with unexpected error: {}", e.getMessage());
            ErrorResponse errorResponse = buildErrorResponse(
                    "An unexpected error occurred during registration", 
                    "INTERNAL_SERVER_ERROR", 
                    request
            );
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
