package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    /**
     * CLI domain field only — not stored in Neon (no email column).
     * Kept on the Java object so account/UI logic can still use it.
     */
    @Transient
    private String email;

    @Column(nullable = false, length = 100)
    private String password;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Account(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}
