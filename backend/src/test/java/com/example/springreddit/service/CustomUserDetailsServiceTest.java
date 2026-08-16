package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * Contains a unit test for the loadUserByUsername() method in CustomUserDetailsService.
 */
@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {
    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;
    @Mock
    private AccountRepository accountRepository;

    /**
     * Load user by username.
     * Expected result: returns UserDetails with fields corresponding to mockAccount.
     */
    @Test
    public void testLoadUserByUsername() {
        Account mockAccount = new Account();
        mockAccount.setId(1L);
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        when(accountRepository.findByUsername("test_user")).thenReturn(Optional.of(mockAccount));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("test_user");

        assertNotNull(userDetails);
        assertEquals("test_user", userDetails.getUsername());
        assertEquals("test_password", userDetails.getPassword());

    }
}
