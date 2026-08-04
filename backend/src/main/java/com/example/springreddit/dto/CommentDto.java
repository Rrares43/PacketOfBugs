package com.example.springreddit.dto;
import com.example.springreddit.shared.ApiResponse;
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
    public record UpdateCommentRequest(
            @NotBlank(message = "Comment cannot be empty")
            @Size(max = 1000, message = "Comment must not exceed 1000 characters")
            String content
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

    @GetMapping("/{id}")
    public ApiResponse<CommentDto.CommentResponse> getComment(@PathVariable UUID id) {
        CommentDto.CommentResponse response = commentService.getCommentById(id);
        return ApiResponse.success(response);
    }
}
