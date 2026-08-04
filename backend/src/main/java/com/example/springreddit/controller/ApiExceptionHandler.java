package com.example.springreddit.controller;

import com.example.springreddit.logging.CustomLogger;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
@Deprecated(since = "1.1", forRemoval = true)
public class ApiExceptionHandler {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

}
