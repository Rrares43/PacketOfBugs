package com.example.springreddit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateCommentRequest(
        @NotBlank(message = "Comment content is required")
        @Size(max = 1000, message = "Comment content must not exceed 1000 characters")
        String content,
        UUID parentId
) {
}
