package com.example.springreddit.controller;

import com.example.springreddit.dto.CommentDto.ApiResponse;
import com.example.springreddit.dto.CommentDto.CommentRequest;
import com.example.springreddit.dto.CommentDto.CommentResponse;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.service.CommentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class CommentController {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping(value = "/comments/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable UUID id) {
        LOGGER.info("Get comment request received for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(commentService.getComment(id)));
    }

    @PostMapping(
            value = "/posts/{postId}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request) {
        LOGGER.info("Create comment request received for post: {} by: {}", postId, request.author());
        CommentResponse comment = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(comment));
    }
}
