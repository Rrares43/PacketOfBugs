package com.example.springreddit.config;

import com.example.springreddit.dto.ErrorResponse;
import com.example.springreddit.exception.*;
import com.example.springreddit.logging.CustomLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            fieldErrors.add(new ErrorResponse.FieldError(
                error.getField(),
                error.getDefaultMessage()
            ))
        );

        LOGGER.warn("Validation error at {}: {} field(s) failed", request.getRequestURI(), fieldErrors.size());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("VALIDATION_ERROR")
                .message("The supplied data is invalid")
                .details(fieldErrors)
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        LOGGER.warn("Resource not found at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("RESOURCE_NOT_FOUND")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            ConflictException ex,
            HttpServletRequest request) {

        LOGGER.warn("Conflict at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("CONFLICT_ERROR")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(
            ForbiddenException ex,
            HttpServletRequest request) {

        LOGGER.warn("Forbidden access at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("FORBIDDEN")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ErrorResponse> handleBusinessLogic(
            BusinessLogicException ex,
            HttpServletRequest request) {

        LOGGER.warn("Business logic error at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("UNPROCESSABLE_ENTITY")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
            UnauthorizedException ex,
            HttpServletRequest request) {

        LOGGER.warn("Unauthorized access at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("UNAUTHORIZED")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(
            RateLimitExceededException ex,
            HttpServletRequest request) {

        LOGGER.warn("Rate limit exceeded at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("RATE_LIMIT_EXCEEDED")
                .message(ex.getMessage())
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request) {

        LOGGER.error("Unexpected error at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
            .success(false)
            .error(ErrorResponse.ErrorDetails.builder()
                .code("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .details(new ArrayList<>())
                .build())
            .timestamp(Instant.now().toString())
            .path(request.getRequestURI())
            .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
