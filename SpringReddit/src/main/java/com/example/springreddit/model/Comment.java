package com.example.springreddit.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Account author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Comment() {
    }

    public Comment(String content, Account author, Post post) {
        this.content = content;
        this.author = author;
        this.post = post;
    }

    public void setContent(String newContent) {
        this.content = newContent;
    }

    public void addReply(Comment reply) {
        this.replies.add(reply);
        reply.setParentComment(this);
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public Account getAuthor() {
        return author;
    }

    public Post getPost() {
        return post;
    }

    public List<Comment> getReplies() {
        return replies;
    }

    public Comment getParentComment() {
        return parentComment;
    }

    public void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}