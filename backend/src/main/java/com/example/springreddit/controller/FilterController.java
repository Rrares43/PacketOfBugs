package com.example.springreddit.controller;

import com.example.springreddit.dto.ApiResponse;
import com.example.springreddit.dto.FilterDto;
import com.example.springreddit.logging.CustomLogger;
import com.example.springreddit.service.FilterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FilterController {
    @Autowired
    private FilterService filterService;
    private final CustomLogger LOGGER = CustomLogger.getInstance();

    @GetMapping("/filters")
    public ResponseEntity<ApiResponse<List<FilterDto>>> getFilters(){
        LOGGER.info("GET /filters request received");
        List<FilterDto> filters = filterService.getAllFilters();
        return ResponseEntity.ok(ApiResponse.success(filters));
    }
}
