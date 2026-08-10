package com.example.springreddit.controller;

import com.example.springreddit.dto.ApiResponse;
import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Post;
import com.example.springreddit.service.PostService;
import com.example.springreddit.service.PostVoteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostsApiController {

    private final PostService postService;
    private final PostVoteService postVoteService;
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    public PostsApiController(PostService postService, PostVoteService postVoteService) {
        this.postService = postService;
        this.postVoteService = postVoteService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto.PostResponse>>> getPosts(
            @RequestParam(required = false) String subreddit) {
        LOGGER.info("GET /posts request received with subreddit filter: {}", subreddit);

        List<Post> posts = (subreddit != null && !subreddit.isBlank())
                ? postService.getPostsBySubreddit(subreddit)
                : postService.getAllPosts();

        String currentUsername = currentUsernameOrNull();
        List<PostDto.PostResponse> postResponses = postService.toPostResponses(posts, currentUsername);

        LOGGER.info("GET /posts request successful, returned {} posts", postResponses.size());
        return ResponseEntity.ok(ApiResponse.success(postResponses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> getPostById(@PathVariable UUID id) {
        LOGGER.info("GET /posts/{} request received", id);

        Post post = postService.getPostById(id);
        String currentUsername = currentUsernameOrNull();
        String userVote = currentUsername != null
                ? postService.resolveUserVote(post, currentUsername)
                : null;
        PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);

        LOGGER.info("GET /posts/{} request successful", id);
        return ResponseEntity.ok(ApiResponse.success(postResponse));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> createPost(
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam String subreddit,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) Integer filter) throws IOException {
        String authorUsername = requireAuthenticatedUsername();

        LOGGER.info(
                "POST /posts request received - title: {}, author: {}, subreddit: {}",
                title, authorUsername, subreddit);

        Post post = postService.createPost(title, content, authorUsername, subreddit, image, filter);
        PostDto.PostResponse postResponse = postService.toPostResponse(post, "up");

        LOGGER.info("POST /posts request successful - created post ID: {}", post.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(postResponse));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request) {
        String currentUsername = requireAuthenticatedUsername();
        LOGGER.info("PUT /posts/{} request received from user: {}", id, currentUsername);

        Post post = postService.updatePost(id, request, currentUsername);
        String userVote = postService.resolveUserVote(post, currentUsername);
        PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);

        LOGGER.info("PUT /posts/{} request successful", id);
        return ResponseEntity.ok(ApiResponse.success(postResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable UUID id) {
        String currentUsername = requireAuthenticatedUsername();
        LOGGER.info("DELETE /posts/{} request received from user: {}", id, currentUsername);

        postService.deletePost(id, currentUsername);

        LOGGER.info("DELETE /posts/{} request successful", id);
        return ResponseEntity.ok(ApiResponse.success("Postarea a fost stearsa cu succes"));
    }

    @PutMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> voteOnPost(
            @PathVariable UUID id,
            @Valid @RequestBody VoteRequest request) {
        String currentUsername = requireAuthenticatedUsername();
        LOGGER.info(
                "PUT /posts/{}/vote request received from user: {} with voteType: {}",
                id, currentUsername, request.voteType());

        VoteResponse voteResponse = postVoteService.voteOnPost(id, currentUsername, request.voteType());

        LOGGER.info("PUT /posts/{}/vote request successful", id);
        return ResponseEntity.ok(ApiResponse.success(voteResponse));
    }
    
    private String currentUsernameOrNull() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return authentication.getName();
    }

    private String requireAuthenticatedUsername() {
        String username = currentUsernameOrNull();
        if (username == null) {
            throw new UnauthorizedException("User not authenticated");
        }
        return username;
    }
}
