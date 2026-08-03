package com.example.springreddit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class CommentDto {

    private CommentDto() {
    }

    public record CommentRequest(
            @NotBlank(message = "Comment content is required")
            @Size(max = 1000, message = "Comment content must not exceed 1000 characters")
            String content,

            @NotBlank(message = "Comment author is required")
            String author,

            UUID parentId
    ) {
    }

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

    public record ApiResponse<T>(boolean success, T data) {
        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, data);
        }
    }
}
