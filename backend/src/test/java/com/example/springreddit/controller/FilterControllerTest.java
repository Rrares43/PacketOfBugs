package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.dto.FilterDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.service.FilterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Contains a unit test for the getFilters() method in FilterController.
 */
@WebMvcTest(FilterController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FilterControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private FilterService filterService;
    @MockitoBean
    private CustomLogger LOGGER = CustomLogger.getInstance();
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserDetailsService userDetailsService;

    /**
     * Get all filters.
     * Expected result: returns the mocked list of filters with no errors.
     * @throws Exception
     */
    @Test
    public void testGetFilters() throws Exception {
        FilterDto mockFilter = new FilterDto(1L, "test_filter", "Test filter");

        List<FilterDto> mockList = new ArrayList<>();
        mockList.add(mockFilter);

        when(filterService.getAllFilters()).thenReturn(mockList);

        mockMvc.perform(get("/filters"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].name").value("test_filter"));
    }
}
