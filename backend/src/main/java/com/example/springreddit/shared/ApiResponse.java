package com.example.springreddit.shared;

import java.time.Instant;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        T data,
        ErrorDetails error,
        Instant timestamp,
        String path
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, Instant.now(), null);
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> error(String message, String code, String path) {
        return (ApiResponse<T>) new ApiResponse<>(
                false,
                null,
                new ErrorDetails(code, message, null),
                Instant.now(),
                path
        );
    }

    @SuppressWarnings("unchecked")
    public static <T> ApiResponse<T> errorWithDetails(String message, String code, String path, List<FieldError> details) {
        return (ApiResponse<T>) new ApiResponse<>(
                false,
                null,
                new ErrorDetails(code, message, details),
                Instant.now(),
                path
        );
    }

    // Backward compatibility - old method signature
    @Deprecated
    public static <T> ApiResponse<T> failure(T data) {
        return new ApiResponse<>(false, data, null, Instant.now(), null);
    }

    public record ErrorDetails(String code, String message, List<FieldError> details) {}

    public record FieldError(String field, String message) {}
}
