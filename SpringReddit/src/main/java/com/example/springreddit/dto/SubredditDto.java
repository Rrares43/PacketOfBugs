package com.example.springreddit.dto;

import lombok.Data;

import java.time.LocalDateTime;

public class SubredditDto {
    @Data
    public static class CreateSubredditRequest{
        private String subredditName;
        private String description;
        private long creatorId;
    }

    @Data
    public static class SubredditResponse{
        private Long id;
        private String name;
        private String description;
        private long creatorId;
        private LocalDateTime createdAt;
    }

}
