package com.example.springreddit.controller;

import com.example.springreddit.logging.LogBuffer;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/logs", produces = MediaType.APPLICATION_JSON_VALUE)
public class LogController {

    private final LogBuffer logBuffer;

    public LogController(LogBuffer logBuffer) {
        this.logBuffer = logBuffer;
    }

    @GetMapping
    public List<String> getLogs() {
        return logBuffer.getLogs();
    }
}
