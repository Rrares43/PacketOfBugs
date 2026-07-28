package com.example.springreddit.dto;

import lombok.Data;

public class PostDto {

    @Data
    public static class CreatePostRequest {
        private String title;
        private String content;
        private Long authorId;
        private String subredditName;
    }

    @Data
    public static class EditPostRequest {
        private String title;
        private String content;
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
    }
}
