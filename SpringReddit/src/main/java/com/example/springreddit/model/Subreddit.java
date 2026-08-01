package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private Account creator;

    @OneToMany(mappedBy = "subreddit", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts = new ArrayList<>();

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
