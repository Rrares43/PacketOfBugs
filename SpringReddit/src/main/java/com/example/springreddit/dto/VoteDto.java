package com.example.springreddit.dto;

import lombok.Data;

public class VoteDto {

    @Data
    public static class VoteRequest {
        private Long accountId;
        /** true = upvote, false = downvote */
        private boolean upvote;
        /**
         * Matches CLI vote menu:
         * 1 = add / change vote direction
         * 2 = remove vote
         */
        private int choice;
    }

    @Data
    public static class VoteResponse {
        private String message;
        private long upvotes;
        private long downvotes;
    }
}
