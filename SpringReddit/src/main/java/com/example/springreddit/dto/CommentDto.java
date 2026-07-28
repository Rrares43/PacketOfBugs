package com.example.springreddit.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentDto {

    @Data
    public static class CreateCommentRequest {
        private String content;
        private Long authorId;
    }

    @Data
    public static class ReplyCommentRequest {
        private String content;
        private Long authorId;
    }

    @Data
    public static class EditCommentRequest {
        private String content;
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
        private long upvotes;
        private long downvotes;
        private LocalDateTime createdAt;
        private List<CommentResponse> replies = new ArrayList<>();
    }
}
