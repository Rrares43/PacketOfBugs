package com.example.springreddit.repository;

import com.example.springreddit.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUsername(String username);
    Account findByEmail(String email);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}

