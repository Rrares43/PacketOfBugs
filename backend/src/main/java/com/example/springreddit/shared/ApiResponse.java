package com.example.springreddit.shared;

public record ApiResponse<T>(
        boolean success,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data);
    }

    public static <T> ApiResponse<T> failure(T data) {
        return new ApiResponse<>(false, data);
    }
}
