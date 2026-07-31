package com.example.springreddit.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PostDto {

    @Data
    public static class CreatePostRequest {
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        private String title;
        @NotBlank(message = "Content cannot be blank")
        private String content;
        @NotNull(message = "Author id is required")
        private Long authorId;
        @NotBlank(message = "Subreddit name cannot be blank")
        private String subredditName;
    }

    @Data
    public static class EditPostRequest {
        @NotBlank(message = "Title cannot be blank")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        private String title;
        @NotBlank(message = "Content cannot be blank")
        private String content;
        @NotNull(message = "Account id is required")
        private Long accountId;
    }

    @Data
    public static class DeletePostRequest {
        private Long accountId;
    }

    @Data
    public static class PostResponse {
        private Long id;
        private String title;
        private String content;
        private Long authorId;
        private String authorUsername;
        private Long subredditId;
        private String subredditName;
        private long upvotes;
        private long downvotes;
        /** 1 = upvoted, -1 = downvoted, 0 = no vote (when accountId is supplied). */
        private int currentUserVote;
    }
}
