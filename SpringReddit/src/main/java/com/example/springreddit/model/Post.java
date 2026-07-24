package com.example.springreddit.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subreddit_id")
    private Subreddit subreddit;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    protected Post() {
    }

    public Post(String title, String content, Account author, Subreddit subreddit) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.subreddit = subreddit;
    }
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public Account getAuthor() { return author; }
    public Subreddit getSubreddit() { return subreddit; }
    public List<Comment> getComments() { return comments; }

    public void addComment(Comment comment) {
        this.comments.add(comment);
    }

    public void editPostContent(String newTitle, String newContent) {
        this.title = newTitle;
        this.content = newContent;
    }
}