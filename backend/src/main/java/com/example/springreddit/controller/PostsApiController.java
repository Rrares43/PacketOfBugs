package com.example.springreddit.controller;

import com.example.springreddit.dto.CommentDto;
import com.example.springreddit.dto.PostDto;
import com.example.springreddit.model.Post;
import com.example.springreddit.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CommentDto.ApiResponse<List<PostDto.PostResponse>>> getPosts(
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
            
            CommentDto.ApiResponse<List<PostDto.PostResponse>> response = 
                    new CommentDto.ApiResponse<>(true, postResponses);
            
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts request successful, returned {} posts", postResponses.size());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "GET /posts request failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(
                    new CommentDto.ApiResponse<>(false, null));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "GET /posts request failed with unexpected error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new CommentDto.ApiResponse<>(false, null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDto.ApiResponse<PostDto.PostResponse>> getPostById(
            @PathVariable UUID id) {
        try {
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts/{} request received", id);
            
            Post post = postService.getPostById(id);
            PostDto.PostResponse postResponse = postService.toPostResponse(post);
            
            com.example.springreddit.logging.CustomLogger.getInstance().info(
                    "GET /posts/{} request successful", id);
            return ResponseEntity.ok(new CommentDto.ApiResponse<>(true, postResponse));
        } catch (IllegalArgumentException e) {
            com.example.springreddit.logging.CustomLogger.getInstance().warn(
                    "GET /posts/{} request failed: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body(
                    new CommentDto.ApiResponse<>(false, null));
        } catch (Exception e) {
            com.example.springreddit.logging.CustomLogger.getInstance().error(
                    "GET /posts/{} request failed with unexpected error: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new CommentDto.ApiResponse<>(false, null));
        }
    }
}
