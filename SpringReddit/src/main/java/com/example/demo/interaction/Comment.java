package com.example.demo.interaction;

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
    private User author;

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

    @Transient
    private VoteTracker voteTracker;

    public Comment() {
        this.voteTracker = new VoteTracker();
    }

    public Comment(String content, User author, Post post) {
        this.content = content;
        this.author = author;
        this.post = post;
        this.voteTracker = new VoteTracker();
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

    public User getAuthor() {
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

    public int getUpvotes() {
        if (voteTracker == null) {
            voteTracker = new VoteTracker();
        }
        return voteTracker.getUpvotes();
    }

    public int getDownvotes() {
        if (voteTracker == null) {
            voteTracker = new VoteTracker();
        }
        return voteTracker.getDownvotes();
    }

    public VoteTracker getVoteTracker() {
        if (this.voteTracker == null) {
            this.voteTracker = new VoteTracker();
        }
        return this.voteTracker;
    }
}