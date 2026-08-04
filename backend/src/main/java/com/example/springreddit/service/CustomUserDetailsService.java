package com.example.springreddit.service;

import com.example.springreddit.model.Account;
import com.example.springreddit.repository.AccountRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    public CustomUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Account account = accountRepository.findByUsername(username)
                .orElseThrow(() -> {
                    com.example.springreddit.logging.CustomLogger.getInstance().warn(
                            "User not found with username: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        
        com.example.springreddit.logging.CustomLogger.getInstance().info(
                "User found with username: {}", username);
        
        return new com.example.springreddit.model.CustomUserDetails(account);
    }
}
