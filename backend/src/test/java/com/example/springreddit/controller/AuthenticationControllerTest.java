package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.mapper.AccountMapper;
import com.example.springreddit.model.Account;
import com.example.springreddit.service.AccountService;
import com.example.springreddit.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AccountMapper accountMapper;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private CustomLogger LOGGER = CustomLogger.getInstance();
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void testLogin() throws Exception {
        AccountDto.LoginRequest request = new AccountDto.LoginRequest();
        request.setUsername("test_user");
        request.setPassword("test_password");

        UserDetails mockUserDetails = User.withUsername("test_user")
                .password("test_password")
                .authorities("USER")
                .build();

        Authentication mockAuthentication = mock(Authentication.class);

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");

        AccountDto.UserInfo mockUser = new AccountDto.UserInfo();
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@email.com");

        AccountDto.AuthResponse response = new AccountDto.AuthResponse();
        response.setUser(mockUser);
        response.setAccessToken("mock_token");

        when(authenticationService.getAuthentication(any(AccountDto.LoginRequest.class)))
                .thenReturn(mockAuthentication);
        when(mockAuthentication.getPrincipal()).thenReturn(mockUserDetails);
        when(jwtTokenProvider.generateToken(mockUserDetails)).thenReturn("mock_token");
        when(accountService.getByUsername("test_user")).thenReturn(mockAccount);
        when(accountMapper.toAuthResponse(any(Account.class), eq("mock_token"))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.username").value("test_user"))
                .andExpect(jsonPath("$.data.accessToken").value("mock_token"));
    }

    @Test
    public void testGetCurrentUser() throws Exception {
        AccountDto.UserProfile mockUserProfile = new AccountDto.UserProfile();
        mockUserProfile.setUsername("test_user");

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");
        when(accountService.getCurrentUserProfile("test_user")).thenReturn(mockUserProfile);

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("test_user"));
    }

    @Test
    public void testUpdateCurrentUser() throws Exception {
        AccountDto.UpdateUserProfileRequest request = new AccountDto.UpdateUserProfileRequest();
        request.setDisplayName("display_name");
        request.setAvatarUrl("avatarurl.com");

        AccountDto.UserProfile mockUserProfile = new AccountDto.UserProfile();
        mockUserProfile.setUsername("test_user");
        mockUserProfile.setDisplayName("display_name");
        mockUserProfile.setAvatarUrl("avatarurl.com");

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");
        when(accountService.updateUserProfile("test_user", request)).thenReturn(mockUserProfile);

        mockMvc.perform(put("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("display_name"))
                .andExpect(jsonPath("$.data.avatarUrl").value("avatarurl.com"));
    }

    @Test
    public void testDeleteUser() throws Exception {
        AccountDto.DeleteAccountRequest request = new AccountDto.DeleteAccountRequest();
        request.setPassword("test_password");

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");

        mockMvc.perform(delete("/auth/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value("Account deleted successfully"));

        verify(accountService, times(1)).deleteAccount("test_user", "test_password");
    }

    @Test
    public void testChangePassword() throws Exception {
        AccountDto.UpdatePasswordRequest request = new AccountDto.UpdatePasswordRequest();
        request.setCurrentPassword("old_password");
        request.setNewPassword("new_password");

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");

        mockMvc.perform(put("/auth/me/password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Password changed successfully"));

        verify(accountService, times(1)).changePassword("test_user", request);
    }

    @Test
    public void testRegister() throws Exception {
        AccountDto.RegistrationRequest request = new AccountDto.RegistrationRequest();
        request.setUsername("test_user");
        request.setPassword("test_password");
        request.setEmail("test@email.com");

        Account mockAccount = new Account();
        mockAccount.setUsername("test_user");
        mockAccount.setPassword("test_password");
        mockAccount.setEmail("test@email.com");

        AccountDto.UserInfo mockUser = new AccountDto.UserInfo();
        mockUser.setUsername("test_user");
        mockUser.setEmail("test@email.com");

        AccountDto.AuthResponse response = new AccountDto.AuthResponse();
        response.setUser(mockUser);
        response.setAccessToken("mock_token");

        when(accountService.registerAccount(request)).thenReturn(mockAccount);
        when(jwtTokenProvider.generateToken(any(UserDetails.class))).thenReturn("mock_token");
        when(accountMapper.toAuthResponse(mockAccount, "mock_token")).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.user.username").value("test_user"))
                .andExpect(jsonPath("$.data.accessToken").value("mock_token"));
    }
}
