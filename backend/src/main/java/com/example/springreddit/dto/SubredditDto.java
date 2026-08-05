package com.example.springreddit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class SubredditDto {
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubredditResponse{
        private UUID id;
        private String name;
        private String displayName;
        private String description;
        private long memberCount;
        private long postCount;
        private String iconUrl;
        private LocalDateTime createdAt;
    }

    public record SubredditListResponse(
       boolean success,
       List<SubredditResponse> subreddits,
       long total
    ){}


    public record CreateSubredditRequest(
        @NotBlank(message = "Subreddit name cannot be blank")
        @Size(min = 3, max = 50, message = "Subreddit name must be between 3-50 characters")
        String name,

        @NotBlank(message = "Subreddit display name cannot be blank")
        @Size(min = 3, max = 100, message = "Subreddit display name must be between 3-100 characters")
        String displayName,

        @NotBlank(message = "Subreddit description cannot be blank")
        @Size(max = 500, message = "Subreddit description must not exceed 500 characters")
        String description,

        @Positive(message = "Creator id is required")
        Long creatorId
        ){}


    public record EditSubredditRequest(
        @NotBlank(message = "Subreddit display name cannot be blank")
        @Size(max = 50, message = "Subreddit display name must not exceed 50 characters")
        String displayName,

        @NotBlank(message = "Subreddit description cannot be blank")
        @Size(max = 250, message = "Subreddit description must not exceed 250 characters")
        String description,

        String iconUrl,

        Long creatorId

        ){}

    public record DeleteSubredditRequest(
            Long creatorId
    ) {}

}
