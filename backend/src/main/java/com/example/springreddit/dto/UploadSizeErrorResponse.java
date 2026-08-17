package com.example.springreddit.dto;

/** Error contract consumed by the existing upload client. */
public record UploadSizeErrorResponse(int status, String message) {
}
