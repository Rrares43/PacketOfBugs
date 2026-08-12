package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto.LoginRequest;
import com.example.springreddit.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 *  Handles operations with JWT authentication tokens
 * @author Denys Dobrynin
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;

    /**
     * Returns an Authentication object for a logged-in user
     * @param loginRequest LoginRequest object with username and password fields
     * @return Authentication object
     */
    public Authentication getAuthentication(LoginRequest loginRequest) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
    }

    /**
     * Returns authenticated user's username from Security context or null if user is not logged in
     * @return String with authenticated user's username
     */
    public String currentUsernameOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }

    /**
     * Returns authenticated user's username and throws an UnauthorizedException if username is null
     * @return String with authenticated user's username
     * @throws UnauthorizedException if the username is null
     */
    public String requireAuthenticatedUsername() {
        String username = currentUsernameOrNull();
        if (username == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return username;
    }
}
