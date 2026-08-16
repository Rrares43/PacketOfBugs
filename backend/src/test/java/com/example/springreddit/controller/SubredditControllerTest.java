package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.SubredditDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.mapper.SubredditMapper;
import com.example.springreddit.model.Account;
import com.example.springreddit.model.Subreddit;
import com.example.springreddit.repository.SubredditSummary;
import com.example.springreddit.service.SubredditService;
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

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a set of unit tests for each method in SubredditController.
 */
@WebMvcTest(SubredditController.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubredditControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private SubredditService subredditService;
    @MockitoBean
    private SubredditMapper subredditMapper;
    @MockitoBean
    private  CustomLogger LOGGER = CustomLogger.getInstance();
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Create a valid subreddit.
     * Expected result: returns the mocked SubredditResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testCreateNewSubreddit() throws Exception {
        SubredditDto.CreateSubredditRequest request = new SubredditDto.CreateSubredditRequest(
                "test_sub",
                "test sub",
                "for testing",
                null
        );

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("test sub");
        mockSubreddit.setDescription("for testing");

        SubredditDto.SubredditResponse response = new SubredditDto.SubredditResponse();
        response.setName("test_sub");
        response.setDisplayName("test sub");
        response.setDescription("for testing");

        when(subredditService.createSubreddit(request)).thenReturn(mockSubreddit);
        when(subredditMapper.mapToResponse(mockSubreddit)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.post("/subreddits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test_sub"));
    }

    /**
     * Get all subreddits.
     * Expected result: returns the list with the mocked SubredditResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testGetAllSubreddits() throws Exception {
        SubredditSummary mockSummary = mock(SubredditSummary.class);
        List<SubredditSummary> mockSummaryList = new ArrayList<>();
        mockSummaryList.add(mockSummary);

        SubredditDto.SubredditResponse response = new SubredditDto.SubredditResponse();
        response.setName("test_sub");;

        when(subredditService.getAllSubredditSummaries()).thenReturn(mockSummaryList);
        when(subredditMapper.mapSummaryToResponse(mockSummary)).thenReturn(response);

        mockMvc.perform(get("/subreddits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("test_sub"));
    }

    /**
     * Get subreddits by the creator's username.
     * Expected result: returns the list with the mocked SubredditResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testGetByCreator() throws Exception {
        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");

        List<Subreddit> mockSubredditList = new ArrayList<>();
        mockSubredditList.add(mockSubreddit);

        SubredditDto.SubredditResponse response = new SubredditDto.SubredditResponse();
        response.setName("test_sub");

        when(subredditService.getSubredditsByCreatorUsername("test_user")).thenReturn(mockSubredditList);
        when(subredditMapper.mapToResponse(mockSubreddit)).thenReturn(response);

        mockMvc.perform(get("/subreddits/by-creator/{username}", "test_user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("test_sub"));
    }

    /**
     * Get subreddit by name.
     * Expected result: returns the mocked SubredditResponse with no errors.
     * @throws Exception
     */
    @Test
    public void testGetSubredditByName() throws Exception {
        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");

        SubredditDto.SubredditResponse response = new SubredditDto.SubredditResponse();
        response.setName("test_sub");

        when(subredditService.getSubredditByName("test_sub")).thenReturn(mockSubreddit);
        when(subredditMapper.mapToResponse(mockSubreddit)).thenReturn(response);

        mockMvc.perform(get("/subreddits/{name}", "test_sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("test_sub"));
    }

    /**
     * Edit an existing subreddit.
     * Expected result: returns the mocked SubredditResponse with no errors
     * @throws Exception
     */
    @Test
    public void testEditSubreddit() throws Exception {
        SubredditDto.EditSubredditRequest request = new SubredditDto.EditSubredditRequest(
                "test sub",
                "for testing",
                "iconurl.com"
        );

        Subreddit mockSubreddit = new Subreddit();
        mockSubreddit.setName("test_sub");
        mockSubreddit.setDisplayName("test sub");
        mockSubreddit.setDescription("for testing");

        SubredditDto.SubredditResponse response = new SubredditDto.SubredditResponse();
        response.setName("test_sub");
        response.setDisplayName("test sub");
        response.setDescription("for testing");

        when(subredditService.editSubreddit("test_sub", request)).thenReturn(mockSubreddit);
        when(subredditMapper.mapToResponse(mockSubreddit)).thenReturn(response);

        mockMvc.perform(MockMvcRequestBuilders.put("/subreddits/{name}", "test_sub")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.displayName").value("test sub"));
    }

    /**
     * Delete a subreddit with no posts.
     * Expected result: subredditService.deleteSubreddit() invoked once.
     * @throws Exception
     */
    @Test
    public void testDeleteSubreddit() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/subreddits/{name}", "test_sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("Subreddit deleted successfully"));

        verify(subredditService, times(1)).deleteSubreddit("test_sub");
    }
}
