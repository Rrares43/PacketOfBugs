package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "posts", indexes = @Index(name = "idx_posts_subreddit", columnList = "subreddit_id"))
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Account author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id")
    private Subreddit subreddit;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @BatchSize(size = 25)
    private List<Comment> comments = new ArrayList<>();

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_status", length = 20)
    private ImageStatus imageStatus;

    @Column(name = "filter_value")
    private Integer filter;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Post(String title, String content, Account author, Subreddit subreddit) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.subreddit = subreddit;
    }

    public Post(String title, String content, Account author, Subreddit subreddit, String imageUrl, Integer filter) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.subreddit = subreddit;
        this.imageUrl = imageUrl;
        this.filter = filter;
    }

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setPost(this);
    }

    public void editPostContent(String newTitle, String newContent) {
        this.title = newTitle;
        this.content = newContent;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
