package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.CommentResponse;
import com.example.springreddit.dto.CreateCommentRequest;
import com.example.springreddit.dto.UpdateCommentRequest;
import com.example.springreddit.dto.VoteRequest;
import com.example.springreddit.dto.VoteResponse;
import com.example.springreddit.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of unit tests for each method in CommentController.
 */
@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    private static final String DELETE_MESSAGE = "The comment was deleted successfully";

    @Test
    public void testGetComment() throws Exception {
        UUID postId = UUID.randomUUID();
        UUID commentId = UUID.randomUUID();
        CommentResponse response = new CommentResponse(
                commentId,
                postId,
                null,
                "test comment",
                "test_user",
                3L,
                2L,
                1L,
                "up",
                Instant.now(),
                Instant.now(),
                new ArrayList<>()
        );

        when(commentService.getComment(response.id())).thenReturn(response);

        mockMvc.perform(get("/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("test comment"));
    }

    @Test
    public void testCreateComment() throws Exception {
        UUID postId = UUID.randomUUID();
        CreateCommentRequest request = new CreateCommentRequest(
                "test_comment",
                null
        );

        CommentResponse response = new CommentResponse(
                UUID.randomUUID(),
                postId,
                null,
                "test comment",
                "test_user",
                3L,
                2L,
                1L,
                "up",
                Instant.now(),
                Instant.now(),
                new ArrayList<>()
        );

        when(commentService.createComment(postId, request)).thenReturn(response);

        mockMvc.perform(post("/posts/{id}/comments", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("test comment"));
    }

    @Test
    public void testUpdateComment() throws Exception {
        UUID commentId = UUID.randomUUID();
        UpdateCommentRequest request = new UpdateCommentRequest("test comment");

        CommentResponse response = new CommentResponse(
                commentId,
                UUID.randomUUID(),
                null,
                "test comment",
                "test_user",
                3L,
                2L,
                1L,
                "up",
                Instant.now(),
                Instant.now(),
                new ArrayList<>()
        );

        when(commentService.updateComment(commentId, request)).thenReturn(response);

        mockMvc.perform(put("/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("test comment"));
    }

    @Test
    public void testDeleteComment() throws Exception {
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(delete("/comments/{id}", commentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.message").value(DELETE_MESSAGE));

        verify(commentService, times(1)).deleteComment(commentId);
    }

    @Test
    public void testVote() throws Exception {
        UUID commentId = UUID.randomUUID();
        VoteRequest request = new VoteRequest("up");

        VoteResponse response = new VoteResponse(
                1L,
                0L,
                1L,
                "up"
        );

        when(commentService.vote(commentId, request)).thenReturn(response);

        mockMvc.perform(put("/comments/{id}/vote", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userVote").value("up"));
    }

    @Test
    public void testGetCommentsByPostId() throws Exception {
        UUID postId = UUID.randomUUID();

        CommentResponse response = new CommentResponse(
                UUID.randomUUID(),
                postId,
                null,
                "test comment",
                "test_user",
                3L,
                2L,
                1L,
                "up",
                Instant.now(),
                Instant.now(),
                new ArrayList<>()
        );

        List<CommentResponse> mockList = new ArrayList<>();
        mockList.add(response);

        when(commentService.getCommentsByPostId(postId)).thenReturn(mockList);

        mockMvc.perform(get("/posts/{id}/comments", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].content").value("test comment"));
    }
}