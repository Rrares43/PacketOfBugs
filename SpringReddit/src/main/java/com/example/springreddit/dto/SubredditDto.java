package com.example.springreddit.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class SubredditDto {
    @Data
    public static class CreateSubredditRequest{
        @NotBlank(message = "Subreddit name cannot be blank")
        @Size(max = 50, message = "Subreddit name must not exceed 50 characters")
        private String subredditName;
        private String description;
        @Positive(message = "Creator id is required")
        private long creatorId;
    }

    @Data
    public static class SubredditResponse{
        private Long id;
        private String name;
        private String description;
        private long creatorId;
        private LocalDateTime createdAt;
        private long postCount;
    }

    @Data
    public static class EditSubredditRequest{
        private String subredditName;
        private String description;
        private long accountId;
    }

    @Data
    public static class DeleteSubredditRequest{
        private long accountId;
    }

}
