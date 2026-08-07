package com.example.springreddit.controller;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.SubredditSummary;
import com.example.springreddit.service.SubredditService;
import com.example.springreddit.dto.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/subreddits", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubredditController {

    @Autowired
    private SubredditService subredditService;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SubredditDto.SubredditResponse>> createNewSubreddit(@Valid @RequestBody SubredditDto.CreateSubredditRequest request) {
        LOGGER.info("Create subreddit request received for subreddit name: {}", request.name());
        Subreddit savedSub = subredditService.createSubreddit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapToResponse(savedSub)));
    }

    @GetMapping
    public ResponseEntity<SubredditDto.SubredditListResponse> getAllSubreddits() {
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getAllSubredditSummaries()
                .stream()
                .map(this::mapSummaryToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new SubredditDto.SubredditListResponse(true, subreddits, subreddits.size()));
    }

    @GetMapping("/by-creator/{username}")
    public ResponseEntity<ApiResponse<List<SubredditDto.SubredditResponse>>> getByCreator(@PathVariable String username) {
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getSubredditsByCreatorUsername(username)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(subreddits));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<SubredditDto.SubredditResponse>> getSubredditByName(@PathVariable String name) {
        LOGGER.info("Get subreddit request received for subreddit name: {}", name);
        Subreddit subreddit = subredditService.getSubredditByName(name);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(subreddit)));
    }

    @GetMapping("/{name}/posts")
    public ResponseEntity<ApiResponse<List<PostDto.PostResponse>>> getPostsBySubreddit(@PathVariable String name) {
        LOGGER.info("Get posts for subreddit request received for subreddit name: {}", name);
        List<PostDto.PostResponse> posts = subredditService.getPostsBySubredditName(name);
        return ResponseEntity.ok(ApiResponse.success(posts));
    }

    @PutMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SubredditDto.SubredditResponse>> editSubreddit(
            @PathVariable String name,
            @Valid @RequestBody SubredditDto.EditSubredditRequest request) {
        LOGGER.info("Edit subreddit request received for subreddit name: {}", name);
        Subreddit editedSubreddit = subredditService.editSubreddit(name, request);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(editedSubreddit)));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ApiResponse<String>> deleteSubreddit(@PathVariable String name) {
        LOGGER.info("Delete subreddit request received for subreddit name: {}", name);
        subredditService.deleteSubreddit(name);
        return ResponseEntity.ok(ApiResponse.success("Subreddit deleted successfully"));
    }

    private SubredditDto.SubredditResponse mapToResponse(Subreddit subreddit) {
        return new SubredditDto.SubredditResponse(
                subreddit.getId(),
                subreddit.getName(),
                subreddit.getDisplayName(),
                subreddit.getDescription(),
                subreddit.getMemberCount(),
                subreddit.getPostCount(),
                subreddit.getIconURL(),
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
