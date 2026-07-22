package com.example.springreddit.dto;

import lombok.Data;

public class AccountDto {

    @Data
    public static class RegistrationRequest{
        private String username;
        private String email;
        private String password;
    }

    @Data
    public static class LoginRequest{
        private String username;
        private String password;
    }

    @Data
    public static class AccountResponse{
        private Long id;
        private String username;
        private String email;
    }
}
