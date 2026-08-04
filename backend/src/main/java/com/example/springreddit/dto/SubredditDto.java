package com.example.springreddit.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public class SubredditDto {
    @Data
    public static class CreateSubredditRequest{
        @NotBlank(message = "Subreddit name cannot be blank")
        @Size(min = 3, max = 50, message = "Subreddit name must be between 3 and 50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Only alphanumeric characters and underscores allowed")
        private String subredditName;
        private String description;
        @Positive(message = "Creator id is required")
        private Long creatorId;
    }

    @Data
    public static class SubredditResponse{
        private Long id;
        private String name;
        private String description;
        private long creatorId;
        private String creatorUsername;
        private LocalDateTime createdAt;
        private long postCount;
    }

    @Data
    public static class EditSubredditRequest{
        private String subredditName;
        private String description;
        private Long accountId;
    }

    @Data
    public static class DeleteSubredditRequest{
        private Long accountId;
    }

}
