package com.example.springreddit.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class CommentVoteId implements Serializable {

    private Long account;
    private UUID comment;

    public CommentVoteId() {
    }

    public CommentVoteId(Long account, UUID comment) {
        this.account = account;
        this.comment = comment;
    }

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long account) {
        this.account = account;
    }

    public UUID getComment() {
        return comment;
    }

    public void setComment(UUID comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CommentVoteId that)) {
            return false;
        }
        return Objects.equals(account, that.account) && Objects.equals(comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, comment);
    }
}
