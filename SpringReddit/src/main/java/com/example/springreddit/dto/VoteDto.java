package com.example.springreddit.dto;

import lombok.Data;

public class VoteDto {

    @Data
    public static class VoteRequest {
        private Long accountId;
        /** true = upvote, false = downvote */
        private boolean upvote;
        private int choice;
    }

    @Data
    public static class VoteResponse {
        private String message;
        private long upvotes;
        private long downvotes;
    }
}
