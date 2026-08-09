package com.example.springreddit.mapper;

import com.example.springreddit.dto.AccountDto;
import com.example.springreddit.model.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto.AccountResponse toResponse(Account account) {
        AccountDto.AccountResponse response = new AccountDto.AccountResponse();
        response.setId(account.getId());
        response.setUsername(account.getUsername());
        response.setEmail(account.getEmail());
        return response;
    }

    public AccountDto.UserInfo toUserInfo(Account account) {
        AccountDto.UserInfo userInfo = new AccountDto.UserInfo();
        userInfo.setUsername(account.getUsername());
        userInfo.setEmail(account.getEmail());
        return userInfo;
    }

    public AccountDto.AuthResponse toAuthResponse(Account account, String accessToken) {
        AccountDto.AuthResponse authResponse = new AccountDto.AuthResponse();
        authResponse.setAccessToken(accessToken);
        authResponse.setUser(toUserInfo(account));
        return authResponse;
    }
}
