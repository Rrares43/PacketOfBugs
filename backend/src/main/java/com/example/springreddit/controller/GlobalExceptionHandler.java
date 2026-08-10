package com.example.springreddit.controller;

import com.example.springreddit.dto.ApiResponse;
import com.example.springreddit.exception.BusinessLogicException;
import com.example.springreddit.exception.ConflictException;
import com.example.springreddit.exception.ForbiddenException;
import com.example.springreddit.exception.RateLimitExceededException;
import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.exception.UnauthorizedException;
import com.example.springreddit.logging.CustomLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception, WebRequest request) {
        List<ApiResponse.FieldError> details = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ApiResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        String path = path(request);
        LOGGER.warn("Validation error on {}: {}", path, details);

        return ResponseEntity.badRequest().body(
                ApiResponse.errorWithDetails(
                        "Datele furnizate nu sunt valide",
                        "VALIDATION_ERROR",
                        path,
                        details
                )
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, WebRequest request) {
        String message = "Invalid request parameter";
        if (exception.getRequiredType() != null
                && UUID.class.isAssignableFrom(exception.getRequiredType())) {
            message = "Invalid UUID format for parameter: " + exception.getName();
        } else if (exception.getName() != null) {
            message = "Invalid value for parameter: " + exception.getName();
        }

        String path = path(request);
        LOGGER.warn("Type mismatch on {}: {}", path, message);

        return ResponseEntity.badRequest().body(
                ApiResponse.error(message, "BAD_REQUEST", path)
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnreadableMessage(
            HttpMessageNotReadableException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Malformed request body on {}: {}", path, exception.getMessage());

        return ResponseEntity.badRequest().body(
                ApiResponse.error("Invalid or malformed JSON payload", "BAD_REQUEST", path)
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            ResourceNotFoundException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Resource not found on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ApiResponse.error(exception.getMessage(), "NOT_FOUND", path)
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflict(
            ConflictException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Conflict on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ApiResponse.error(exception.getMessage(), "CONFLICT", path)
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            ForbiddenException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Forbidden on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error(exception.getMessage(), "FORBIDDEN", path)
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            UnauthorizedException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Unauthorized on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(exception.getMessage(), "UNAUTHORIZED", path)
        );
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessLogic(
            BusinessLogicException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Business logic error on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ApiResponse.error(exception.getMessage(), "UNPROCESSABLE_ENTITY", path)
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleRateLimitExceeded(
            RateLimitExceededException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Rate limit exceeded on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                ApiResponse.error(exception.getMessage(), "RATE_LIMIT_EXCEEDED", path)
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            IllegalArgumentException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Bad request on {}: {}", path, exception.getMessage());

        String message = exception.getMessage() != null ? exception.getMessage() : "Bad request";
        String code = "BAD_REQUEST";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        if (message.toLowerCase().contains("already exists")) {
            code = "CONFLICT";
            status = HttpStatus.CONFLICT;
        } else if (message.toLowerCase().contains("not found")) {
            code = "NOT_FOUND";
            status = HttpStatus.NOT_FOUND;
        }

        return ResponseEntity.status(status).body(
                ApiResponse.error(message, code, path)
        );
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handleSecurityException(
            SecurityException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Security exception on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(exception.getMessage(), "UNAUTHORIZED", path)
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Authentication failed on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ApiResponse.error(exception.getMessage(), "UNAUTHORIZED", path)
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Access denied on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ApiResponse.error("Access denied", "FORBIDDEN", path)
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxSizeException(
            MaxUploadSizeExceededException exception, WebRequest request) {
        String path = path(request);
        LOGGER.warn("Max upload size exceeded on {}: {}", path, exception.getMessage());

        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(
                ApiResponse.error("File too large", "PAYLOAD_TOO_LARGE", path)
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception exception, WebRequest request) {
        String path = path(request);
        LOGGER.error("Unexpected error on {}: {}", path, exception.getMessage(), exception);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("An unexpected error occurred", "INTERNAL_SERVER_ERROR", path)
        );
    }

    private static String path(WebRequest request) {
        return ((ServletWebRequest) request).getRequest().getRequestURI();
    }
}
