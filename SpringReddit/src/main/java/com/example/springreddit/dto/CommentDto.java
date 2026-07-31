package com.example.springreddit.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentDto {

    @Data
    public static class CreateCommentRequest {
        @NotBlank(message = "Comment content cannot be blank")
        @Size(max = 3000, message = "Comment content must not exceed 3000 characters")
        private String content;
        @NotNull(message = "Author id is required")
        private Long authorId;
    }

    @Data
    public static class ReplyCommentRequest {
        @NotBlank(message = "Reply content cannot be blank")
        @Size(max = 3000, message = "Reply content must not exceed 3000 characters")
        private String content;
        @NotNull(message = "Author id is required")
        private Long authorId;
    }

    @Data
    public static class EditCommentRequest {
        @NotBlank(message = "Comment content cannot be blank")
        @Size(max = 3000, message = "Comment content must not exceed 3000 characters")
        private String content;
        @NotNull(message = "Account id is required")
        private Long accountId;
    }

    @Data
    public static class DeleteCommentRequest {
        private Long accountId;
    }

    @Data
    public static class CommentResponse {
        private Long id;
        private String content;
        private Long authorId;
        private String authorUsername;
        private Long postId;
        private Long parentCommentId;
        private boolean deleted;
        private LocalDateTime deletedAt;
        private long upvotes;
        private long downvotes;
        private LocalDateTime createdAt;
        private List<CommentResponse> replies = new ArrayList<>();
    }
}
