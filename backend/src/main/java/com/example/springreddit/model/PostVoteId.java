package com.example.springreddit.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


public class PostVoteId implements Serializable {

    private Long account;
    private UUID post;

    public PostVoteId() {
    }

    public PostVoteId(Long account, UUID post) {
        this.account = account;
        this.post = post;
    }

    public Long getAccount() {
        return account;
    }

    public void setAccount(Long account) {
        this.account = account;
    }

    public UUID getPost() {
        return post;
    }

    public void setPost(UUID post) {
        this.post = post;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PostVoteId that)) {
            return false;
        }
        return Objects.equals(account, that.account) && Objects.equals(post, that.post);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, post);
    }
}
