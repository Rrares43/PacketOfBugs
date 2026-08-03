package com.example.springreddit.dto;

import com.example.springreddit.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

public class AccountDto {

    @Data
    public static class RegistrationRequest {
        @NotBlank(message = "Username cannot be blank")
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
        private String username;
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
        private String email;

        @NotBlank(message = "New password cannot be blank")
        @ValidPassword
        private String newPassword;
    }
}
