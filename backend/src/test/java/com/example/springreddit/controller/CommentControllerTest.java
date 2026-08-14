package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.*;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.service.CommentService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CustomLogger LOGGER = CustomLogger.getInstance();
    @MockitoBean
    private CommentService commentService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
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
                new ArrayList<CommentResponse>()
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
                new ArrayList<CommentResponse>()
        );

        when(commentService.createComment(postId, request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/posts/{id}/comments", postId)
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
                new ArrayList<CommentResponse>()
        );

        when(commentService.updateComment(commentId, request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/comments/{id}", commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").value("test comment"));
    }

    @Test
    public void testDeleteComment() throws Exception {
        UUID commentId = UUID.randomUUID();

        mockMvc.perform(MockMvcRequestBuilders.delete("/comments/{id}", commentId))
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
                1,
                0,
                1,
                "up"
        );

        when(commentService.vote(commentId, request)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/comments/{id}/vote", commentId)
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
                new ArrayList<CommentResponse>()
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
