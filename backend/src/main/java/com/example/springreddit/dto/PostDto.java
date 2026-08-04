package com.example.springreddit.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public class PostDto {

    @Data
    public static class CreatePostRequest {
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 3, max = 300, message = "The title must be between 3 and 300 characters")
        private String title;
        @Size(max = 10000, message = "Content must not exceed 10000 characters")
        private String content;
        @NotBlank(message = "Subreddit name cannot be blank")
        @Size(min = 3, max = 50, message = "Subreddit name must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Only alphanumeric characters and underscores allowed")
        private String subredditName;
    }

    @Data
    public static class EditPostRequest {
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 3, max = 300, message = "The title must be between 3 and 300 characters")
        private String title;
        @Size(max = 10000, message = "Content must not exceed 10000 characters")
        private String content;
        @NotNull(message = "Account id is required")
        private Long accountId;
    }

    @Data
    public static class DeletePostRequest {
        private Long accountId;
    }

    public record PostResponse(
            UUID id,
            String title,
            String content,
            String imageUrl,
            Integer filter,
            String author,
            String subreddit,
            long upvotes,
            long downvotes,
            long score,
            long commentCount,
            String userVote,
            String createdAt,
            String updatedAt
    ) {}
}
