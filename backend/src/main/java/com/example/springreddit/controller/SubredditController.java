package com.example.springreddit.controller;

import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.service.SubredditService;
import com.example.springreddit.repository.SubredditSummary;
import com.example.springreddit.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/subreddits", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubredditController {

    @Autowired
    private SubredditService subredditService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createNewSubreddit(@Valid @RequestBody SubredditDto.CreateSubredditRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Create subreddit request received for subreddit name: {} by creator ID: {}", request.name(), request.creatorId());
            Subreddit savedSub = subredditService.createSubreddit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapToResponse(savedSub)));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create subreddit failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<SubredditDto.SubredditResponse>> getAllSubreddits() {
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getAllSubredditSummaries()
                .stream()
                .map(this::mapSummaryToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new SubredditDto.SubredditListResponse(true, subreddits, subreddits.size()).subreddits());
    }

    @GetMapping("/by-creator/{username}")
    public ResponseEntity<List<SubredditDto.SubredditResponse>> getByCreator(@PathVariable String username) {
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getSubredditsByCreatorUsername(username)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(subreddits);
    }

    @GetMapping("/{name}")
    public ResponseEntity<?> getSubredditByName(@PathVariable String name) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Get subreddit request received for subreddit name: {}", name);
            Subreddit subreddit = subredditService.getSubredditByName(name);
            return ResponseEntity.ok(ApiResponse.success(mapToResponse(subreddit)));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Get subreddit failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editSubreddit(@PathVariable String name,
                                           @RequestBody SubredditDto.EditSubredditRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Edit subreddit request received for subreddit ID: {} by account ID: {}", name, request.creatorId());
            Subreddit editedSubreddit = subredditService.editSubreddit(name, request);
            return ResponseEntity.ok(ApiResponse.success(mapToResponse(editedSubreddit)));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (SecurityException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit subreddit failed - security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> deleteSubreddit(@PathVariable String name, @RequestBody SubredditDto.DeleteSubredditRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Delete subreddit request received for subreddit name: {} by account ID: {}", name, request.creatorId());
            subredditService.deleteSubreddit(name, request.creatorId());
            return ResponseEntity.ok(ApiResponse.success("Subreddit deleted successfully"));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
        catch (SecurityException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete subreddit failed - security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    private SubredditDto.SubredditResponse mapToResponse(Subreddit subreddit) {
        return new SubredditDto.SubredditResponse(
                subreddit.getId(),
                subreddit.getName(),
                subreddit.getDisplayName(),
                subreddit.getDescription(),
                subreddit.getMemberCount(),
                subreddit.getPostCount(),
                subreddit.getIconURl(),
                subreddit.getCreatedAt()
        );
    }

    private SubredditDto.SubredditResponse mapSummaryToResponse(SubredditSummary summary) {
        return new SubredditDto.SubredditResponse(
            summary.getId(),
            summary.getName(),
            summary.getDisplayName(),
            summary.getDescription(),
            summary.getMemberCount(),
            summary.getPostCount(),
            summary.getIconURL(),
            summary.getCreatedAt()
        );
    }
}
