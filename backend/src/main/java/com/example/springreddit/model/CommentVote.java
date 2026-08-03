package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "comment_votes")
@IdClass(CommentVoteId.class)
@Getter
@Setter
@NoArgsConstructor
public class CommentVote {

    public static final short UPVOTE = 1;
    public static final short DOWNVOTE = -1;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @Column(name = "vote_type", nullable = false)
    private short voteType;

    public CommentVote(Comment comment, Account account, short voteType) {
        this.comment = comment;
        this.account = account;
        setVoteType(voteType);
    }

    public CommentVote(Comment comment, Account account, int voteDirection) {
        this(comment, account, (short) voteDirection);
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
        setVoteType(upvote ? UPVOTE : DOWNVOTE);
    }
}
