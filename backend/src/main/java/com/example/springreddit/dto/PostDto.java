package com.example.springreddit.dto;

import java.util.UUID;

public class PostDto {

    public record PostResponse(
            UUID id,
            String title,
            String content,
            String imageUrl,
            String imageStatus,
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
