package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.PostDto;
import com.example.springreddit.dto.UpdatePostRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.model.Post;
import com.example.springreddit.service.AuthenticationService;
import com.example.springreddit.service.PostService;
import com.example.springreddit.service.PostVoteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of unit tests for each method in PostController.
 */
@WebMvcTest(PostController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PostService postService;
    @MockitoBean
    private PostVoteService postVoteService;
    @MockitoBean
    private AuthenticationService authenticationService;
    @MockitoBean
    private CustomLogger LOGGER = CustomLogger.getInstance();
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Get all posts.
     * Expected result: returns the mocked list of PostResponse objects with no errors.
     * @throws Exception
     */
    @Test
    public void testGetPosts() throws Exception {
        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("test post");
        mockPost.setContent("test content");

        List<Post> mockList = new ArrayList<>();
        mockList.add(mockPost);

        PostDto.PostResponse response = new PostDto.PostResponse(
                postId,
                "test post",
                "test content",
                null,
                null,
                null,
                "test_user",
                "test_sub",
                1L,
                0L,
                1L,
                0L,
                "up",
                Instant.now().toString(),
                Instant.now().toString()
        );

        List<PostDto.PostResponse> mockResponseList = new ArrayList<>();
        mockResponseList.add(response);

        when(postService.getAllPostsOrBySubreddit(null)).thenReturn(mockList);
        when(authenticationService.currentUsernameOrNull()).thenReturn("test_user");
        when(postService.toPostResponses(mockList, "test_user")).thenReturn(mockResponseList);

        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("test post"));
    }

    /**
     * Get posts by subreddit name.
     * Expected result: returns the mocked list of PostResponse objects with no errors.
     * @throws Exception
     */
    @Test
    public void testGetSubredditPosts() throws Exception {
        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("test post");
        mockPost.setContent("test content");

        List<Post> mockList = new ArrayList<>();
        mockList.add(mockPost);

        PostDto.PostResponse response = new PostDto.PostResponse(
                postId,
                "test post",
                "test content",
                null,
                null,
                null,
                "test_user",
                "test_sub",
                1L,
                0L,
                1L,
                0L,
                "up",
                Instant.now().toString(),
                Instant.now().toString()
        );

        List<PostDto.PostResponse> mockResponseList = new ArrayList<>();
        mockResponseList.add(response);

        when(postService.getPostsBySubreddit("test_sub")).thenReturn(mockList);
        when(authenticationService.currentUsernameOrNull()).thenReturn("test_user");
        when(postService.toPostResponses(mockList, "test_user")).thenReturn(mockResponseList);

        mockMvc.perform(get("/subreddits/{name}/posts", "test_sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].title").value("test post"));
    }

    /**
     * Get post by ID.
     * Expected result: returns the mocked PostResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testGetPostById() throws Exception {
        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("test post");
        mockPost.setContent("test content");

        PostDto.PostResponse response = new PostDto.PostResponse(
                postId,
                "test post",
                "test content",
                null,
                null,
                null,
                "test_user",
                "test_sub",
                1L,
                0L,
                1L,
                0L,
                "up",
                Instant.now().toString(),
                Instant.now().toString()
        );

        when(postService.getPostById(postId)).thenReturn(mockPost);
        when(authenticationService.currentUsernameOrNull()).thenReturn("test_user");
        when(postService.resolveUserVote(mockPost, "test_user")).thenReturn("up");
        when(postService.toPostResponse(mockPost, "up")).thenReturn(response);

        mockMvc.perform(get("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("test post"));
    }

    /**
     * Create a valid post.
     * Expected result: returns the mocked PostResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testCreatePost() throws Exception {
        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("test post");
        mockPost.setContent("test content");

        PostDto.PostResponse response = new PostDto.PostResponse(
                postId,
                "test post",
                "test content",
                null,
                null,
                null,
                "test_user",
                "test_sub",
                1L,
                0L,
                1L,
                0L,
                "up",
                Instant.now().toString(),
                Instant.now().toString()
        );

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");
        when(postService.createPost("test post",
                "test content",
                "test_user",
                "test_sub",
                null,
                null)).thenReturn(mockPost);
        when(postService.toPostResponse(mockPost, "up")).thenReturn(response).thenReturn(response);

        mockMvc.perform(multipart("/posts")
                .param("title", "test post")
                .param("content", "test content")
                .param("subreddit", "test_sub"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("test post"));
    }

    /**
     * Update an existing post.
     * Expected result: return the mocked postResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testUpdatePost() throws Exception {
        UUID postId = UUID.randomUUID();
        Post mockPost = new Post();
        mockPost.setId(postId);
        mockPost.setTitle("test post");
        mockPost.setContent("test content");

        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("test post");
        request.setContent("test content");

        PostDto.PostResponse response = new PostDto.PostResponse(
                postId,
                "test post",
                "test content",
                null,
                null,
                null,
                "test_user",
                "test_sub",
                1L,
                0L,
                1L,
                0L,
                "up",
                Instant.now().toString(),
                Instant.now().toString()
        );

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");
        when(postService.updatePost(postId, request, "test_user")).thenReturn(mockPost);
        when(postService.resolveUserVote(mockPost, "test_user")).thenReturn("up");
        when(postService.toPostResponse(mockPost, "up")).thenReturn(response).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("test post"));
    }

    /**
     * Delete an existing post.
     * Expected result: postService.deletePost() invoked once.
     * @throws Exception
     */
    @Test
    public void testDeletePost() throws Exception {
        UUID postId = UUID.randomUUID();

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");

        mockMvc.perform(MockMvcRequestBuilders.delete("/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Postarea a fost stearsa cu succes"));

        verify(postService, times(1)).deletePost(postId, "test_user");
    }

    /**
     * Leave a valid vote on a post.
     * Expected result: returns the mocked VoteResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testVoteOnPost() throws Exception {
        UUID postId = UUID.randomUUID();
        VoteRequest request = new VoteRequest("up");
        VoteResponse response = new VoteResponse(
                1,
                0,
                1,
                "up"
        );

        when(authenticationService.requireAuthenticatedUsername()).thenReturn("test_user");
        when(postVoteService.voteOnPost(postId, "test_user", "up")).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/posts/{id}/vote", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userVote").value("up"));
    }
}