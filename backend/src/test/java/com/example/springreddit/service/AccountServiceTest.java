package com.example.springreddit.service;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Contains a set of unit tests for each method in AccountService.
 */
@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @InjectMocks
    private AccountService accountService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    /**
     * Register a valid account.
     * Expected result: returns mockAccount, passwordEncoder.encode() and accountRepository.save() invoked with no errors.
     */
    @Test
    public void testRegisterAccount() {
        AccountDto.RegistrationRequest mockRequest = new AccountDto.RegistrationRequest();
        mockRequest.setUsername("test_user");
        mockRequest.setPassword("test_password");
        mockRequest.setEmail("test@email.com");

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("hashed_password");
        mockAccount.setEmail("test@email.com");

        when(passwordEncoder.encode("test_password")).thenReturn("hashed_password");
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        Account createdAccount = accountService.registerAccount(mockRequest);
        assertNotNull(createdAccount);
        assertEquals(mockAccount, createdAccount);

        verify(passwordEncoder).encode("test_password");
        verify(accountRepository).save(any(Account.class));
    }

    /**
     * Get an existing account by username.
     * Expected result: returns mockAccount with no errors.
     */
    @Test
    public void testGetByUsername() {
        Account mockAccount = new Account();
        mockAccount.setId((long)1);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");
        mockAccount.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));

        Account retrievedAccount = accountService.getByUsername("test_user");
        assertEquals(mockAccount, retrievedAccount);
    }

    /**
     * Get current user profile.
     * Expected result: returns the UserProfile with fields corresponding to mockAccount with no errors.
     */
    @Test
    public void testGetCurrentUserProfile() {
        Account mockAccount = new Account();
        mockAccount.setId((long)1);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");
        mockAccount.setCreatedAt(LocalDateTime.now());
        mockAccount.setDisplayName("Test User");
        mockAccount.setAvatarUrl("avatarurl.com");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));

        AccountDto.UserProfile userProfile = accountService.getCurrentUserProfile("test_user");
        assertEquals("test_user", userProfile.getUsername());
        assertEquals("Test User", userProfile.getDisplayName());
        assertEquals("test@email.com", userProfile.getEmail());
        assertEquals("avatarurl.com", userProfile.getAvatarUrl());
    }

    /**
     * Update an existing user account.
     * Expected result: mockAccount updated with no errors.
     */
    @Test
    public void testUpdateUserProfile() {
        AccountDto.UpdateUserProfileRequest mockRequest = new AccountDto.UpdateUserProfileRequest();
        mockRequest.setDisplayName("New Test User");
        mockRequest.setAvatarUrl("newavatarurl.com");

        Account mockAccount = new Account();
        mockAccount.setId((long)1);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");
        mockAccount.setCreatedAt(LocalDateTime.now());
        mockAccount.setDisplayName("Test User");
        mockAccount.setAvatarUrl("avatarurl.com");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        AccountDto.UserProfile userProfile = accountService.updateUserProfile("test_user", mockRequest);
        assertEquals("New Test User", userProfile.getDisplayName());
        assertEquals("newavatarurl.com", userProfile.getAvatarUrl());
    }

    /**
     * Change the password for an existing account.
     * Expected result: password changed for mockAccount with no errors.
     */
    @Test
    public void testChangePassword() {
        AccountDto.UpdatePasswordRequest mockRequest = new AccountDto.UpdatePasswordRequest();
        mockRequest.setCurrentPassword("test_password");
        mockRequest.setNewPassword("new_test_password");

        Account mockAccount = new Account();
        mockAccount.setId((long)1);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");
        mockAccount.setCreatedAt(LocalDateTime.now());

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(passwordEncoder.encode("new_test_password")).thenReturn("new_hashed_password");
        when(passwordEncoder.matches(mockRequest.getCurrentPassword(), mockAccount.getPassword())).thenReturn(true);

        accountService.changePassword("test_user", mockRequest);
        assertEquals("new_hashed_password", mockAccount.getPassword());
        verify(passwordEncoder).encode("new_test_password");
        verify(accountRepository).save(any(Account.class));
    }

    /**
     * Delete an existing account.
     * Expected result: mockAccount deleted with no errors.
     */
    @Test
    public void testDeleteAccount() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");
        mockAccount.setCreatedAt(LocalDateTime.now());

        when(accountRepository.existsByUsername("test_user")).thenReturn(true);
        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));
        doAnswer(invocation -> {
                mockAccount.setDeleted(true);
                return null;
                }).when(accountRepository).deleteByUsername("test_user");
        when(passwordEncoder.matches("test_password", mockAccount.getPassword())).thenReturn(true);

        accountService.deleteAccount("test_user", "test_password");

        assertTrue(mockAccount.isDeleted());
    }
}