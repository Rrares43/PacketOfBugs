package com.example.springreddit.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CommentResponse(
        UUID id,
        UUID postId,
        UUID parentId,
        String content,
        String author,
        long upvotes,
        long downvotes,
        long score,
        String userVote,
        Instant createdAt,
        Instant updatedAt,
        List<CommentResponse> replies
) {
    public CommentResponse {
        replies = replies == null ? List.of() : List.copyOf(replies);
    }
}
