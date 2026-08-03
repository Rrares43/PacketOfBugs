package com.example.springreddit.controller;

import com.example.springreddit.exception.ResourceNotFoundException;
import com.example.springreddit.logging.CustomLogger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        LOGGER.warn("Validation error: {}", fields);
        return ResponseEntity.badRequest().body(error("Validation failed", fields));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(ResourceNotFoundException exception) {
        LOGGER.warn("Resource not found: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(error(HttpStatus.NOT_FOUND.value(), exception.getMessage(), Map.of()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> badRequest(IllegalArgumentException exception) {
        LOGGER.warn("Bad request: {}", exception.getMessage());
        return ResponseEntity.badRequest().body(error(exception.getMessage(), Map.of()));
    }

    private Map<String, Object> error(String message, Map<String, String> fields) {
        return error(HttpStatus.BAD_REQUEST.value(), message, fields);
    }

    private Map<String, Object> error(int status, String message, Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", status);
        body.put("message", message);
        if (!fields.isEmpty()) body.put("fields", fields);
        return body;
    }
}
