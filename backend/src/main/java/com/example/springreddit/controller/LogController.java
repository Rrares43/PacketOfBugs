package com.example.springreddit.controller;

import com.example.springreddit.logging.CustomLogger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/logs", produces = MediaType.APPLICATION_JSON_VALUE)
public class LogController {

    @GetMapping
    public List<String> getLogs() {
        return CustomLogger.getInstance().getLogs();
    }
}
