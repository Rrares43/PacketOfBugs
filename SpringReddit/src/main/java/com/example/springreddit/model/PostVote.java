package com.example.springreddit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_votes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"post_id", "account_id"})
})
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean upvote; // true pentru upvote, false pentru downvote

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    protected PostVote() {
    }

    public PostVote(boolean upvote, Post post, Account account) {
        this.upvote = upvote;
        this.post = post;
        this.account = account;
    }

    public Long getId() {
        return id;
    }

    public boolean isUpvote() {
        return upvote;
    }

    public void setUpvote(boolean upvote) {
        this.upvote = upvote;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}