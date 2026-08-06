package com.example.springreddit.controller;

import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.shared.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> validation(MethodArgumentNotValidException exception, WebRequest request) {
        List<ApiResponse.FieldError> details = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ApiResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Validation error on {}: {}", path, details);
        
        ApiResponse<Void> response = ApiResponse.errorWithDetails(
                "Datele furnizate nu sunt valide",
                "VALIDATION_ERROR",
                path,
                details
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> notFound(ResourceNotFoundException exception, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Resource not found on {}: {}", path, exception.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                exception.getMessage(),
                "NOT_FOUND",
                path
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> badRequest(IllegalArgumentException exception, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Bad request on {}: {}", path, exception.getMessage());
        
        String code = "BAD_REQUEST";
        if (exception.getMessage().contains("already exists")) {
            code = "CONFLICT";
        } else if (exception.getMessage().contains("not found") || exception.getMessage().contains("Not found")) {
            code = "NOT_FOUND";
        }
        
        ApiResponse<Void> response = ApiResponse.error(
                exception.getMessage(),
                code,
                path
        );
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> unauthorized(SecurityException exception, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Unauthorized access on {}: {}", path, exception.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                exception.getMessage(),
                "UNAUTHORIZED",
                path
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> forbidden(org.springframework.security.access.AccessDeniedException exception, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.warn("Access denied on {}: {}", path, exception.getMessage());
        
        ApiResponse<Void> response = ApiResponse.error(
                "Access denied",
                "FORBIDDEN",
                path
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception exception, WebRequest request) {
        String path = ((ServletWebRequest) request).getRequest().getRequestURI();
        LOGGER.error("Unexpected error on {}: {}", path, exception.getMessage(), exception);
        
        ApiResponse<Void> response = ApiResponse.error(
                "An unexpected error occurred",
                "INTERNAL_SERVER_ERROR",
                path
        );
        return ResponseEntity.internalServerError().body(response);
    }
}
