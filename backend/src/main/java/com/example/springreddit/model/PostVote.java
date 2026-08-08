package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "post_votes", indexes = {
        @Index(name = "idx_post_votes_post_account", columnList = "post_id, account_id")
})
@IdClass(PostVoteId.class)
@Getter
@Setter
@NoArgsConstructor
public class PostVote {

    public static final short UPVOTE = 1;
    public static final short DOWNVOTE = -1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "vote_type", nullable = false)
    private short voteType;

    public PostVote(Account account, Post post, short voteType) {
        this.account = account;
        this.post = post;
        setVoteType(voteType);
    }

    public PostVote(boolean upvote, Post post, Account account) {
        this(account, post, upvote ? UPVOTE : DOWNVOTE);
    }

    public void setVoteType(short voteType) {
        if (voteType != UPVOTE && voteType != DOWNVOTE) {
            throw new IllegalArgumentException("vote_type must be 1 (upvote) or -1 (downvote)");
        }
        this.voteType = voteType;
    }

    public boolean isUpvote() {
        return voteType == UPVOTE;
    }

    public void setUpvote(boolean upvote) {
        this.voteType = upvote ? UPVOTE : DOWNVOTE;
    }
}
