package com.example.springreddit.controller;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.CustomUserDetails;
import com.example.springreddit.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of integration tests for each method in AuthenticationController.
 */
@AutoConfigureMockMvc
public class AuthenticationControllerIT extends BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Register a valid account.
     * Expected result: account persists with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testRegister() throws Exception {
        AccountDto.RegistrationRequest request = new AccountDto.RegistrationRequest();
        request.setUsername("test_user");
        request.setPassword("test_password");
        request.setEmail("test@email.com");


        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("test_user"))
                .andExpect(jsonPath("$.data.accessToken").exists());

        assertTrue(accountRepository.existsByUsername("test_user"));
    }

    /**
     * Login into an existent account.
     * Expected result: returns an AuthResponse object with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testLogin() throws Exception {
        AccountDto.LoginRequest request = new AccountDto.LoginRequest();
        request.setUsername("test_user");
        request.setPassword("test_password");

        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");

        accountRepository.save(account);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("test_user"))
                .andExpect(jsonPath("$.data.accessToken").exists());
    }

    /**
     * Get the logged-in user profle from the security context.
     * Expected result: returns the logged-in UserProfile with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testGetCurrentUser() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");

        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        mockMvc.perform(get("/auth/me")
                        .with(user(details)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("test_user"));
    }

    /**
     * Update the logged-in account.
     * Expected result: updates the logged in account with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testUpdateCurrentUser() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        account.setDisplayName("Test user");

        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        AccountDto.UpdateUserProfileRequest request = new AccountDto.UpdateUserProfileRequest();
        request.setDisplayName("New name");
        request.setAvatarUrl("avatarurl.com");

        mockMvc.perform(put("/auth/me")
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("New name"))
                .andExpect(jsonPath("$.data.avatarUrl").value("avatarurl.com"));

        Account updatedAccount = accountRepository.findByUsername("test_user")
                .orElseThrow(() -> new Exception());

        assertEquals("New name", updatedAccount.getDisplayName());
    }

    /**
     * Delete the logged-in account.
     * Expected result: account deleted with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testDeleteUser() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        account.setDisplayName("Test user");

        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        AccountDto.DeleteAccountRequest request = new AccountDto.DeleteAccountRequest();
        request.setPassword("test_password");

        mockMvc.perform(delete("/auth/me")
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Account deleted successfully"));

        Account deletedAccount = accountRepository.findByUsername("test_user")
                .orElseThrow(() -> new Exception());

        assertTrue(deletedAccount.isDeleted());
    }

    /**
     * Change the password for the logged-in account.
     * Expected result: password changed with no errors.
     * @throws Exception
     */
    @Test
    @Transactional
    public void testChangePassword() throws Exception {
        Account account = new Account();
        account.setUsername("test_user");
        account.setPassword(passwordEncoder.encode("test_password"));
        account.setEmail("test@email.com");
        account.setDisplayName("Test user");

        accountRepository.save(account);
        UserDetails details = new CustomUserDetails(account);

        AccountDto.UpdatePasswordRequest request = new AccountDto.UpdatePasswordRequest();
        request.setCurrentPassword("test_password");
        request.setNewPassword("new_password");

        mockMvc.perform(put("/auth/me/password")
                        .with(user(details))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Password changed successfully"));

        Account updatedAccount = accountRepository.findByUsername("test_user")
                .orElseThrow(() -> new Exception());

        assertTrue(passwordEncoder.matches("new_password", updatedAccount.getPassword()));
    }
}
