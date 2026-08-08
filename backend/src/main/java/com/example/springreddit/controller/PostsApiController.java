package com.example.springreddit.controller;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.exception.ForbiddenException;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Post;
import com.example.springreddit.service.PostService;
import com.example.springreddit.service.PostVoteService;
import com.example.springreddit.dto.ApiResponse;
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
        try {
            LOGGER.info(
                    "GET /posts request received with subreddit filter: {}", subreddit);
            
            List<Post> posts;
            if (subreddit != null && !subreddit.isBlank()) {
                posts = postService.getPostsBySubreddit(subreddit);
            } else {
                posts = postService.getAllPosts();
            }
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            final String currentUsername = authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
                    ? authentication.getName()
                    : null;
            
            List<PostDto.PostResponse> postResponses = postService.toPostResponses(posts, currentUsername);
            
            LOGGER.info(
                    "GET /posts request successful, returned {} posts", postResponses.size());
            return ResponseEntity.ok(ApiResponse.success(postResponses));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "GET /posts request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts"));
        } catch (Exception e) {
            LOGGER.error(
                    "GET /posts request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDto.PostResponse>> getPostById(
            @PathVariable UUID id) {
        try {
            LOGGER.info(
                    "GET /posts/{} request received", id);
            
            Post post = postService.getPostById(id);
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            final String currentUsername = authentication != null && !(authentication instanceof AnonymousAuthenticationToken)
                    ? authentication.getName()
                    : null;
            
            String userVote = currentUsername != null 
                    ? postService.resolveUserVote(post, currentUsername) 
                    : null;
            PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);
            
            LOGGER.info(
                    "GET /posts/{} request successful", id);
            return ResponseEntity.ok(ApiResponse.success(postResponse));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "GET /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id));
        } catch (Exception e) {
            LOGGER.error(
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
                LOGGER.warn("POST /posts request failed: user not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts"));
            }
            String authorUsername = authentication.getName();
            
            LOGGER.info(
                    "POST /posts request received - title: {}, author: {}, subreddit: {}", 
                    title, authorUsername, subreddit);
            
            Post post = postService.createPost(title, content, authorUsername, subreddit, image, filter);
            PostDto.PostResponse postResponse = postService.toPostResponse(post, "up");
            
            LOGGER.info(
                    "POST /posts request successful - created post ID: {}", post.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    ApiResponse.success(postResponse));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "POST /posts request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts"));
        } catch (IOException e) {
            LOGGER.error(
                    "POST /posts request failed - image upload error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts"));
        } catch (Exception e) {
            LOGGER.error(
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
                LOGGER.warn(
                        "PUT /posts/{} request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id));
            }
            String currentUsername = authentication.getName();

            LOGGER.info(
                    "PUT /posts/{} request received from user: {}", id, currentUsername);

            Post post = postService.updatePost(id, request, currentUsername);
            String userVote = postService.resolveUserVote(post, currentUsername);
            PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);

            LOGGER.info(
                    "PUT /posts/{} request successful", id);
            return ResponseEntity.ok(ApiResponse.success(postResponse));
        } catch (ResourceNotFoundException e) {
            LOGGER.warn(
                    "PUT /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(e.getMessage(), "NOT_FOUND", "/posts/" + id));
        } catch (ForbiddenException e) {
            LOGGER.warn(
                    "PUT /posts/{} request failed - forbidden: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage(), "FORBIDDEN", "/posts/" + id));
        } catch (UnauthorizedException e) {
            LOGGER.warn(
                    "PUT /posts/{} request failed - unauthorized: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/posts/" + id));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "PUT /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id));
        } catch (Exception e) {
            LOGGER.error(
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
                LOGGER.warn(
                        "DELETE /posts/{} request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id));
            }
            String currentUsername = authentication.getName();

            LOGGER.info(
                    "DELETE /posts/{} request received from user: {}", id, currentUsername);

            postService.deletePost(id, currentUsername);

            LOGGER.info(
                    "DELETE /posts/{} request successful", id);
            return ResponseEntity.ok(ApiResponse.success("Postarea a fost stearsa cu succes"));
        } catch (ResourceNotFoundException e) {
            LOGGER.warn(
                    "DELETE /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(e.getMessage(), "NOT_FOUND", "/posts/" + id));
        } catch (ForbiddenException e) {
            LOGGER.warn(
                    "DELETE /posts/{} request failed - forbidden: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage(), "FORBIDDEN", "/posts/" + id));
        } catch (UnauthorizedException e) {
            LOGGER.warn(
                    "DELETE /posts/{} request failed - unauthorized: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/posts/" + id));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "DELETE /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id));
        } catch (Exception e) {
            LOGGER.error(
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
                LOGGER.warn(
                        "PUT /posts/{}/vote request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id + "/vote"));
            }
            String currentUsername = authentication.getName();

            LOGGER.info(
                    "PUT /posts/{}/vote request received from user: {} with voteType: {}", 
                    id, currentUsername, request.voteType());

            VoteResponse voteResponse = postVoteService.voteOnPost(id, currentUsername, request.voteType());

            LOGGER.info(
                    "PUT /posts/{}/vote request successful", id);
            return ResponseEntity.ok(ApiResponse.success(voteResponse));
        } catch (ResourceNotFoundException e) {
            LOGGER.warn(
                    "PUT /posts/{}/vote request failed: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ApiResponse.error(e.getMessage(), "NOT_FOUND", "/posts/" + id + "/vote"));
        } catch (ForbiddenException e) {
            LOGGER.warn(
                    "PUT /posts/{}/vote request failed - forbidden: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    ApiResponse.error(e.getMessage(), "FORBIDDEN", "/posts/" + id + "/vote"));
        } catch (UnauthorizedException e) {
            LOGGER.warn(
                    "PUT /posts/{}/vote request failed - unauthorized: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getMessage(), "UNAUTHORIZED", "/posts/" + id + "/vote"));
        } catch (IllegalArgumentException e) {
            LOGGER.warn(
                    "PUT /posts/{}/vote request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getMessage(), "BAD_REQUEST", "/posts/" + id + "/vote"));
        } catch (Exception e) {
            LOGGER.error(
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
                LOGGER.warn(
                        "PUT /posts/{}/apply-filter request failed: user not authenticated", id);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                        ApiResponse.error("User not authenticated", "UNAUTHORIZED", "/posts/" + id + "/apply-filter"));
            }
            String currentUsername = authentication.getName();

            LOGGER.info(
                    "PUT /posts/{}/apply-filter request received from user: {}", id, currentUsername, filter);
            Post post = postService.applyFilterToPost(id, filter, currentUsername);

            String userVote = postService.resolveUserVote(post, currentUsername);
            PostDto.PostResponse postResponse = postService.toPostResponse(post, userVote);

            LOGGER.info(
                    "PUT /posts/{}/apply-filter request successful", id);
            return ResponseEntity.ok(ApiResponse.success(postResponse));

        } catch (IOException | ResourceNotFoundException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {
            LOGGER.error(
                    "PUT /posts/{}/apply-filter request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error(e.getMessage(), "INTERNAL_SERVER_ERROR", "/posts/" + id + "/apply-filter"));
        }
    }
}
