package com.example.springreddit.controller;

import com.example.springreddit.dto.ApiResponse;
import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.mapper.SubredditMapper;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.service.SubredditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/subreddits", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class SubredditController {

    private final SubredditService subredditService;
    private final SubredditMapper subredditMapper;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SubredditDto.SubredditResponse>> createNewSubreddit(
            @Valid @RequestBody SubredditDto.CreateSubredditRequest request) {
        LOGGER.info("Create subreddit request received for subreddit name: {}", request.name());
        Subreddit savedSub = subredditService.createSubreddit(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(subredditMapper.mapToResponse(savedSub)));
    }

    @GetMapping
    public ResponseEntity<SubredditDto.SubredditListResponse> getAllSubreddits() {
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getAllSubredditSummaries()
                .stream()
                .map(subredditMapper::mapSummaryToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new SubredditDto.SubredditListResponse(true, subreddits, subreddits.size()));
    }

    @GetMapping("/by-creator/{username}")
    public ResponseEntity<ApiResponse<List<SubredditDto.SubredditResponse>>> getByCreator(
            @PathVariable String username) {
        List<SubredditDto.SubredditResponse> subreddits = subredditService.getSubredditsByCreatorUsername(username)
                .stream()
                .map(subredditMapper::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(subreddits));
    }

    @GetMapping("/{name}")
    public ResponseEntity<ApiResponse<SubredditDto.SubredditResponse>> getSubredditByName(
            @PathVariable String name) {
        LOGGER.info("Get subreddit request received for subreddit name: {}", name);
        Subreddit subreddit = subredditService.getSubredditByName(name);
        return ResponseEntity.ok(ApiResponse.success(subredditMapper.mapToResponse(subreddit)));
    }
    
    @PutMapping(value = "/{name}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<SubredditDto.SubredditResponse>> editSubreddit(
            @PathVariable String name,
            @Valid @RequestBody SubredditDto.EditSubredditRequest request) {
        LOGGER.info("Edit subreddit request received for subreddit name: {}", name);
        Subreddit editedSubreddit = subredditService.editSubreddit(name, request);
        return ResponseEntity.ok(ApiResponse.success(subredditMapper.mapToResponse(editedSubreddit)));
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<ApiResponse<String>> deleteSubreddit(@PathVariable String name) {
        LOGGER.info("Delete subreddit request received for subreddit name: {}", name);
        subredditService.deleteSubreddit(name);
        return ResponseEntity.ok(ApiResponse.success("Subreddit deleted successfully"));
    }
}
