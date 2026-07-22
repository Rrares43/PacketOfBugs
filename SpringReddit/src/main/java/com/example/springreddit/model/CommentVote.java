package com.example.springreddit.model;

import jakarta.persistence.*;

@Entity
@Table(name = "comment_votes")
public class CommentVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "vote_direction", nullable = false)
    private int voteDirection;

    public CommentVote() {}

    public CommentVote(Comment comment, Account account, int voteDirection) {
        this.comment = comment;
        this.account = account;
        this.voteDirection = voteDirection;
    }

    public Long getId() { return id; }
    public Comment getComment() { return comment; }
    public Account getAccount() { return account; }
    public int getVoteDirection() { return voteDirection; }

    public void setVoteDirection(int voteDirection) { this.voteDirection = voteDirection; }
}