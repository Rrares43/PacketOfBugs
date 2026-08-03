package com.example.springreddit.controller;

import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.VoteDto;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Post;
import com.example.springreddit.service.AccountService;
import com.example.springreddit.service.PostService;
import com.example.springreddit.service.PostVoteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private final PostService postService;
    private final PostVoteService postVoteService;
    private final AccountService accountService;

    public PostController(PostService postService,
                          PostVoteService postVoteService,
                          AccountService accountService) {
        this.postService = postService;
        this.postVoteService = postVoteService;
        this.accountService = accountService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createPost(@Valid @RequestBody PostDto.CreatePostRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Create post request received for subreddit: {} by author ID: {}", request.getSubredditName(), request.getAuthorId());
            Post post = postService.createPost(
                    request.getTitle(),
                    request.getContent(),
                    request.getAuthorId(),
                    request.getSubredditName());
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(post));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Create post failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<PostDto.PostResponse>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts().stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPost(@PathVariable UUID id) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Get post request received for post ID: {}", id);
            return ResponseEntity.ok(toResponse(postService.getPostById(id)));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Get post failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @GetMapping("/subreddit/{name}")
    public ResponseEntity<List<PostDto.PostResponse>> getBySubreddit(@PathVariable String name) {
        return ResponseEntity.ok(postService.getPostsBySubreddit(name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> editPost(@PathVariable UUID id, @Valid @RequestBody PostDto.EditPostRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Edit post request received for post ID: {} by account ID: {}", id, request.getAccountId());
            Post post = postService.editPost(id, request.getTitle(), request.getContent(), request.getAccountId());
            return ResponseEntity.ok(toResponse(post));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit post failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Edit post failed - security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> deletePost(@PathVariable UUID id, @RequestBody PostDto.DeletePostRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Delete post request received for post ID: {} by account ID: {}", id, request.getAccountId());
            postService.deletePost(id, request.getAccountId());
            return ResponseEntity.ok("Post deleted successfully");
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Delete post failed - security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping(value = "/{id}/votes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> vote(@PathVariable UUID id, @Valid @RequestBody VoteDto.VoteRequest request) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info("Vote request received for post ID: {} by account ID: {} - upvote: {}", id, request.getAccountId(), request.isUpvote());
            Account account = accountService.getById(request.getAccountId());
            String message = postVoteService.vote(id, account, request.isUpvote(), request.getChoice());

            VoteDto.VoteResponse response = new VoteDto.VoteResponse();
            response.setMessage(message);
            response.setUpvotes(postVoteService.countUpvotes(id));
            response.setDownvotes(postVoteService.countDownvotes(id));
            response.setCurrentUserVote(postVoteService.currentVote(id, request.getAccountId()));
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn("Vote failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private PostDto.PostResponse toResponse(Post post) {
        PostDto.PostResponse response = new PostDto.PostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        if (post.getAuthor() != null) {
            response.setAuthorId(post.getAuthor().getId());
            response.setAuthorUsername(post.getAuthor().getUsername());
        }
        if (post.getSubreddit() != null) {
            response.setSubredditId(post.getSubreddit().getId());
            response.setSubredditName(post.getSubreddit().getName());
        }
        response.setUpvotes(postService.countUpvotes(post));
        response.setDownvotes(postService.countDownvotes(post));
        return response;
    }
}
