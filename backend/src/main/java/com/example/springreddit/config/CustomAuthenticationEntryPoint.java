package com.example.springreddit.config;

import com.example.springreddit.logging.CustomLogger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/*
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        LOGGER.warn(
                "Unauthorized request - {}: {}", request.getRequestURI(), authException.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"Buggit Authentication required before main functions of the App \",\"data\":null}");
    }
}

 */
