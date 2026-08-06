package com.example.springreddit.controller;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.exception.ForbiddenException;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.model.Post;
import com.example.springreddit.service.PostService;
import com.example.springreddit.shared.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostsApiController {

    private final PostService postService;

    public PostsApiController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostDto.PostResponse>>> getPosts(
            @RequestParam(required = false) String subreddit) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts request received with subreddit filter: {}", subreddit);
            
            List<Post> posts;
            if (subreddit != null && !subreddit.isBlank()) {
                posts = postService.getPostsBySubreddit(subreddit);
            } else {
                posts = postService.getAllPosts();
            }
            
            List<PostDto.PostResponse> postResponses = posts.stream()
                    .map(postService::toPostResponse)
                    .collect(Collectors.toList());
            
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts request successful, returned {} posts", postResponses.size());
            return ResponseEntity.ok(ApiResponse.success(postResponses));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "GET /posts request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts"));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "GET /posts request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> getPostById(
            @PathVariable UUID id) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts/{} request received", id);
            
            Post post = postService.getPostById(id);
            PostDto.PostResponse postResponse = postService.toPostResponse(post);
            
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts/{} request successful", id);
            return ResponseEntity.ok(ApiResponse.success(postResponse));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "GET /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "GET /posts/{} request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts/" + id));
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> createPost(
            @RequestParam String title,
            @RequestParam(required = false) String content,
            @RequestParam String subreddit,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) Integer filter) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn("POST /posts request failed: user not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts"));
            }
            String authorUsername = authentication.getName();
            
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "POST /posts request received - title: {}, author: {}, subreddit: {}", 
                    title, authorUsername, subreddit);
            
            Post post = postService.createPost(title, content, authorUsername, subreddit, image, filter);
            PostDto.PostResponse postResponse = postService.toPostResponse(post, "up");
            
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "POST /posts request successful - created post ID: {}", post.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(postResponse));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "POST /posts request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts"));
        } catch (IOException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "POST /posts request failed - image upload error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts"));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "POST /posts request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts"));
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> updatePost(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePostRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "PUT /posts/{} request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id));
            }
            String currentUsername = authentication.getName();

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /posts/{} request received from user: {}", id, currentUsername);

            Post post = postService.updatePost(id, request, currentUsername);
            String userVote = postService.resolveUserVote(post, currentUsername);
            PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /posts/{} request successful", id);
            return ResponseEntity.ok(ApiResponse.success(postResponse));
        } catch (ResourceNotFoundException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(e.getMessage(), "NOT_FOUND", "/posts/" + id));
        } catch (ForbiddenException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{} request failed - forbidden: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage(), "FORBIDDEN", "/posts/" + id));
        } catch (UnauthorizedException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{} request failed - unauthorized: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/posts/" + id));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "PUT /posts/{} request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts/" + id));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable UUID id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "DELETE /posts/{} request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id));
            }
            String currentUsername = authentication.getName();

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "DELETE /posts/{} request received from user: {}", id, currentUsername);

            postService.deletePost(id, currentUsername);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "DELETE /posts/{} request successful", id);
            return ResponseEntity.ok(ApiResponse.success("Postarea a fost stearsa cu succes"));
        } catch (ResourceNotFoundException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "DELETE /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(e.getMessage(), "NOT_FOUND", "/posts/" + id));
        } catch (ForbiddenException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "DELETE /posts/{} request failed - forbidden: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage(), "FORBIDDEN", "/posts/" + id));
        } catch (UnauthorizedException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "DELETE /posts/{} request failed - unauthorized: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/posts/" + id));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "DELETE /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "DELETE /posts/{} request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts/" + id));
        }
    }

    @PutMapping("/{id}/vote")
    public ResponseEntity<ApiResponse<VoteResponse>> voteOnPost(
            @PathVariable UUID id,
            @Valid @RequestBody VoteRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "PUT /posts/{}/vote request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id + "/vote"));
            }
            String currentUsername = authentication.getName();

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /posts/{}/vote request received from user: {} with voteType: {}", 
                    id, currentUsername, request.voteType());

            VoteResponse voteResponse = postService.voteOnPost(id, currentUsername, request.voteType());

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /posts/{}/vote request successful", id);
            return ResponseEntity.ok(ApiResponse.success(voteResponse));
        } catch (ResourceNotFoundException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{}/vote request failed: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(e.getMessage(), "NOT_FOUND", "/posts/" + id + "/vote"));
        } catch (ForbiddenException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{}/vote request failed - forbidden: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage(), "FORBIDDEN", "/posts/" + id + "/vote"));
        } catch (UnauthorizedException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{}/vote request failed - unauthorized: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/posts/" + id + "/vote"));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "PUT /posts/{}/vote request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id + "/vote"));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "PUT /posts/{}/vote request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts/" + id + "/vote"));
        }
    }

    @PutMapping("/{id}/apply-filter")
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> applyFilter(
            @PathVariable UUID id,
            @RequestParam String filter) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                com.example.springreddit.logging.CustomLogger.getInstance().warn(
                        "PUT /posts/{}/apply-filter request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id + "/apply-filter"));
            }
            String currentUsername = authentication.getName();

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /posts/{}/apply-filter request received from user: {}", id, currentUsername, filter);
            Post post = postService.applyFilterToPost(id, filter, currentUsername);

            String userVote = postService.resolveUserVote(post, currentUsername);
            PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);

            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "PUT /posts/{}/apply-filter request successful", id);
            return ResponseEntity.ok(ApiResponse.success(postResponse));

        } catch (IOException | ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "PUT /posts/{}/apply-filter request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts/" + id + "/apply-filter"));
        }
    }
}
