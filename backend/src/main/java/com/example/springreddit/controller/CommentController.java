package com.example.springreddit.controller;

import com.example.springreddit.dto.CommentDto.CommentRequest;
import com.example.springreddit.dto.CommentDto.CommentResponse;
import com.example.springreddit.dto.CommentDto.UpdateCommentRequest;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.service.CommentService;
import com.example.springreddit.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable UUID id) {
        LOGGER.info("Get comment request received for ID: {}", id);
        CommentResponse response = commentService.getComment(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping(
            value = "/posts/{postId}/comments",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CommentRequest request) {
        LOGGER.info("Create comment request received for post: {} by: {}", postId, request.author());
        CommentResponse comment = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(comment));
    }

    @PutMapping(
            value = "/comments/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request) {
        LOGGER.info("Update comment request received for ID: {}", id);
        CommentResponse updatedComment = commentService.updateComment(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedComment));
    }
}