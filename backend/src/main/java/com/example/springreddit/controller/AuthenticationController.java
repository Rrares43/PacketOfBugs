package com.example.springreddit.controller;

import com.example.springreddit.annotation.RateLimit;
import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.dto.ApiResponse;
import com.example.springreddit.dto.DeleteAccountResponse;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.mapper.AccountMapper;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @RateLimit
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AccountDto.AuthResponse>> login(
            @Valid @RequestBody AccountDto.LoginRequest loginRequest, TimeZone timeZone) {
        LOGGER.info("Login attempt for username: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtTokenProvider.generateToken(userDetails);

        Account account = accountService.getByUsername(loginRequest.getUsername());
        AccountDto.AuthResponse authResponse = accountMapper.toAuthResponse(account, jwt);

        LOGGER.info("Login successful for username: {}", loginRequest.getUsername());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto.UserProfile>> getCurrentUser() {
        String username = requireAuthenticatedUsername();
        LOGGER.info("GET /auth/me request received for username: {}", username);

        AccountDto.UserProfile userProfile = accountService.getCurrentUserProfile(username);

        LOGGER.info("GET /auth/me request successful for username: {}", username);
        return ResponseEntity.ok(ApiResponse.success(userProfile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<AccountDto.UserProfile>> updateCurrentUser(
            @Valid @RequestBody AccountDto.UpdateUserProfileRequest request) {
        String username = requireAuthenticatedUsername();
        LOGGER.info("PUT /auth/me request received for username: {} with request: {}", username, request);

        AccountDto.UserProfile userProfile = accountService.updateUserProfile(username, request);

        LOGGER.info("PUT /auth/me request successful for username: {}", username);
        return ResponseEntity.ok(ApiResponse.success(userProfile));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<DeleteAccountResponse>> deleteUser(
            @Valid @RequestBody AccountDto.DeleteAccountRequest request) {

        String username = requireAuthenticatedUsername();
        accountService.deleteAccount(username, request.getPassword());

        return ResponseEntity.ok(ApiResponse.success(new DeleteAccountResponse(true, "Account deleted successfully")));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> changePassword(
            @Valid @RequestBody AccountDto.UpdatePasswordRequest request) {
        String username = requireAuthenticatedUsername();
        LOGGER.info("PUT /auth/me/password request received for username: {}", username);

        accountService.changePassword(username, request);

        LOGGER.info("PUT /auth/me/password request successful for username: {}", username);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AccountDto.AuthResponse>> register(
            @Valid @RequestBody AccountDto.RegistrationRequest registrationRequest) {
        LOGGER.info("Registration attempt for username: {}", registrationRequest.getUsername());

        Account account = accountService.registerAccount(registrationRequest);

        UserDetails userDetails = new CustomUserDetails(account);
        String jwt = jwtTokenProvider.generateToken(userDetails);
        AccountDto.AuthResponse authResponse = accountMapper.toAuthResponse(account, jwt);

        LOGGER.info("Registration successful for username: {}", account.getUsername());
        return ResponseEntity.ok(ApiResponse.success(authResponse));
    }

    private String requireAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User not authenticated");
        }
        return authentication.getName();
    }
}
