package com.example.springreddit.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        com.example.springreddit.logging.CustomLogger.getInstance().warn("Validation error: {}", fields);
        return ResponseEntity.badRequest().body(error("Validation failed", fields));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> integrityViolation(DataIntegrityViolationException exception) {
        com.example.springreddit.logging.CustomLogger.getInstance().error("Data integrity violation occurred", exception);
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(error("The request conflicts with existing data", Map.of()));
    }

    private Map<String, Object> error(String message, Map<String, String> fields) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("message", message);
        if (!fields.isEmpty()) body.put("fields", fields);
        return body;
    }
}
