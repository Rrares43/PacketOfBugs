package com.example.springreddit.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "comments")
@Getter
@Setter
@NoArgsConstructor
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

    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CommentVote> votes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public Comment(String content, Account author, Post post) {
        this.content = Objects.requireNonNull(content, "content must not be null");
        this.author = Objects.requireNonNull(author, "author must not be null");
        this.post = Objects.requireNonNull(post, "post must not be null");
    }

    public void editContent(String newContent) {
        this.content = Objects.requireNonNull(newContent, "content must not be null");
    }

    public void addReply(Comment reply) {
        Objects.requireNonNull(reply, "reply must not be null");
        if (!Objects.equals(this.post.getId(), reply.getPost().getId())) {
            throw new IllegalArgumentException("Reply must belong to the same post as its parent");
        }
        this.replies.add(reply);
        reply.setParentComment(this);
    }

    public boolean belongsToPost(Long postId) {
        return post != null && Objects.equals(post.getId(), postId);
    }

    public boolean isAuthoredBy(Account account) {
        return author != null && account != null && Objects.equals(author.getId(), account.getId());
    }

    public List<Comment> getReplies() {
        return Collections.unmodifiableList(replies);
    }
}
