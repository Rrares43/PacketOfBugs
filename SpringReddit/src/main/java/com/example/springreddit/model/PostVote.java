package com.example.springreddit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "post_votes")
public class PostVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "vote_direction", nullable = false)
    private int voteDirection;

    public PostVote() {
    }

    public PostVote(Post post, Account account, int voteDirection) {
        this.post = post;
        this.account = account;
        this.voteDirection = voteDirection;
    }

    public Long getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public Account getAccount() {
        return account;
    }

    public int getVoteDirection() {
        return voteDirection;
    }

    public void setVoteDirection(int voteDirection) {
        this.voteDirection = voteDirection;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}