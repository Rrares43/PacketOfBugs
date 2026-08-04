package com.example.springreddit.dto;

public record VoteResponse(
        long upvotes,
        long downvotes,
        long score,
        String userVote
) {
}
