package com.example.springreddit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class SubredditDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubredditResponse {
        private UUID id;
        private String name;
        private String displayName;
        private String description;
        private long memberCount;
        private long postCount;
        private String iconUrl;
        private Instant createdAt;
    }

    public record SubredditListResponse(
        List<SubredditResponse> subreddits,
        long total
    ) {}

    public record CreateSubredditRequest(
        @NotBlank(message = "Subreddit name cannot be blank")
        @Size(min = 3, max = 50, message = "Subreddit name must be between 3-50 characters")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Subreddit name must be alphanumeric with underscore only")
        String name,

        @NotBlank(message = "Subreddit display name cannot be blank")
        @Size(min = 3, max = 100, message = "Subreddit display name must be between 3-100 characters")
        String displayName,

        @NotBlank(message = "Subreddit description cannot be blank")
        @Size(max = 500, message = "Subreddit description must not exceed 500 characters")
        String description,

        String iconUrl
    ) {}

    public record EditSubredditRequest(
        @Size(min = 3, max = 100, message = "Display name must be between 3-100 characters")
        String displayName,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        String iconUrl
    ) {}
}
