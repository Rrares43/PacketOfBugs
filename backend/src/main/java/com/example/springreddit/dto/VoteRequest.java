package com.example.springreddit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VoteRequest(
        @NotBlank(message = "Vote type is required")
        @Pattern(regexp = "up|down|none", message = "Vote type must be up, down, or none")
        String voteType
) {
}
