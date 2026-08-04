package com.example.springreddit.controller;

import com.example.springreddit.dto.CommentResponse;
import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.DeleteCommentResponse;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.service.CommentService;
import com.example.springreddit.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();
    private static final String DELETE_MESSAGE = "The comment was deleted successfully";

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<CommentResponse>> getComment(@PathVariable UUID id) {
        LOGGER.info("Get comment request received for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(commentService.getComment(id)));
    }

    @PostMapping(value = "/posts/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @PathVariable UUID postId,
            @Valid @RequestBody CreateCommentRequest request) {
        LOGGER.info("Create comment request received for post: {}", postId);
        CommentResponse comment = commentService.createComment(postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(comment));
    }

    @PutMapping(value = "/comments/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCommentRequest request) {
        LOGGER.info("Update comment request received for ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(commentService.updateComment(id, request)));
    }

    @DeleteMapping("/comments/{id}")
    public ResponseEntity<ApiResponse<DeleteCommentResponse>> deleteComment(@PathVariable UUID id) {
        LOGGER.info("Delete comment request received for ID: {}", id);
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success(new DeleteCommentResponse(true, DELETE_MESSAGE)));
    }

    @PutMapping(value = "/comments/{id}/vote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<CommentResponse>> vote(
            @PathVariable UUID id,
            @Valid @RequestBody VoteRequest request) {
        LOGGER.info("Vote request received for comment ID: {}", id);
        return ResponseEntity.ok(ApiResponse.success(commentService.vote(id, request)));
    }
}
