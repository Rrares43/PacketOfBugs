package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "subreddits")
@Getter
@Setter
@NoArgsConstructor
public class Subreddit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Account creator;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Subreddit(String name, String description, Account creator) {
        setName(name);
        this.description = description;
        this.creator = creator;
    }

    public void setName(String name) {
        if (name != null && !name.startsWith("r/")) {
            this.name = "r/" + name;
        } else {
            this.name = name;
        }
    }
}
