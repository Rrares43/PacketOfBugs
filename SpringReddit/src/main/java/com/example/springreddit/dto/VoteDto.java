package com.example.springreddit.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class VoteDto {

    @Data
    public static class VoteRequest {
        @NotNull(message = "Account id is required")
        private Long accountId;
        /** true = upvote, false = downvote */
        private boolean upvote;
        @Min(value = 1, message = "Choice must be 1 (add) or 2 (remove)")
        @Max(value = 2, message = "Choice must be 1 (add) or 2 (remove)")
        private int choice;
    }

    @Data
    public static class VoteResponse {
        private String message;
        private long upvotes;
        private long downvotes;
        /** 1 = upvoted, -1 = downvoted, 0 = no vote. */
        private int currentUserVote;
    }
}
