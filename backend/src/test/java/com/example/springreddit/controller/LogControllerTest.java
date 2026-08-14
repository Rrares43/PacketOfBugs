package com.example.springreddit.controller;

import com.example.springreddit.config.JwtTokenProvider;
import com.example.springreddit.logging.CustomLogger;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
@AutoConfigureMockMvc(addFilters = false)
public class LogControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void testGetLogs() throws Exception {
        List<String> mockList = new ArrayList<>();
        mockList.add("test_log");

        CustomLogger mockLoggerInstance = mock(CustomLogger.class);

        try (MockedStatic<CustomLogger> mockedStatic = mockStatic(CustomLogger.class)) {
            mockedStatic.when(CustomLogger::getInstance).thenReturn(mockLoggerInstance);
            when(mockLoggerInstance.getLogs()).thenReturn(mockList);

            mockMvc.perform(get("/logs"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0]").value("test_log"));
        }
    }
}
