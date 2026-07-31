package com.example.springreddit.model;

import java.io.Serializable;
import java.util.Objects;

public class CommentVoteId implements Serializable {

    private Long account;
    private Long comment;

    public CommentVoteId() {
    }

    public CommentVoteId(Long account, Long comment) {
        this.account = account;
        this.comment = comment;
    }

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long account) {
        this.account = account;
    }

    public Long getComment() {
        return comment;
    }

    public void setComment(Long comment) {
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
