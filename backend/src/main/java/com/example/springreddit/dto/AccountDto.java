package com.example.springreddit.dto;

import com.example.springreddit.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class AccountDto {

    @Data
    public static class RegistrationRequest {
        @NotBlank(message = "Username cannot be blank")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Only alphanumeric characters and underscores allowed")
        private String username;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "Password cannot be blank")
        @ValidPassword
        private String password;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "Username cannot be blank")
        private String username;

        @NotBlank(message = "Password cannot be blank")
        private String password;
    }

    @Data
    public static class AccountResponse {
        private Long id;
        private String username;
        private String email;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank(message = "Username cannot be blank")
        private String username;

        @NotBlank(message = "Email cannot be blank")
        @Email(message = "Invalid email format")
        private String email;

        @NotBlank(message = "New password cannot be blank")
        @ValidPassword
        private String newPassword;
    }

    @Data
    public static class UserInfo {
        private String username;
        private String email;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private UserInfo user;
    }

    @Data
    public static class UserProfile {
        private String username;
        private String email;
        private String displayName;
        private String avatarUrl;
    }

    @Data
    public static class UpdateUserProfileRequest {
        private String displayName;
        private String avatarUrl;
    }

    @Data
    public static class UpdatePasswordRequest {
        @NotBlank(message = "Current password cannot be blank")
        private String currentPassword;

        @NotBlank(message = "New password cannot be blank")
        @ValidPassword
        private String newPassword;
    }
}
