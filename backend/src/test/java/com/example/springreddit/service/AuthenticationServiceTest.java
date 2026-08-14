package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.exception.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {
    @InjectMocks
    private AuthenticationService authenticationService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testGetAuthentication() {
        SecurityContextHolder.setContext(securityContext);
        AccountDto.LoginRequest mockRequest = new AccountDto.LoginRequest();
        mockRequest.setUsername("test_user");
        mockRequest.setPassword("test_password");

        Authentication mockAuthentication = new UsernamePasswordAuthenticationToken(
                "test_user",
                "test_password"
        );

        when(authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                "test_user",
                "test_password"
        ))).thenReturn(mockAuthentication);

        Authentication newAuthentication = authenticationService.getAuthentication(mockRequest);
        assertNotNull(newAuthentication);
        assertEquals(mockAuthentication, newAuthentication);
    }

    @Test
    public void testCurrentUsernameOrNull() {
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");
        when(authentication.getPrincipal()).thenReturn("test_user");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (org.mockito.MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = org.mockito.Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            String username = authenticationService.currentUsernameOrNull();

            assertEquals("test_user", username);
        }
    }

    @Test
    void requireAuthenticatedUsername_ReturnsUsername_WhenAuthenticated() {
        SecurityContextHolder.setContext(securityContext);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test_user");
        when(authentication.getPrincipal()).thenReturn("test_user");
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (org.mockito.MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = org.mockito.Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            String username = authenticationService.requireAuthenticatedUsername();

            assertEquals("test_user", username);
        }
    }

    @Test
    void requireAuthenticatedUsername_ThrowsException_WhenNotAuthenticated() {
        SecurityContextHolder.setContext(securityContext);


        when(authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (org.mockito.MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = org.mockito.Mockito.mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            UnauthorizedException exception = assertThrows(
                    UnauthorizedException.class,
                    () -> authenticationService.requireAuthenticatedUsername()
            );

            assertEquals("User not authenticated", exception.getMessage());
        }
    }
}
