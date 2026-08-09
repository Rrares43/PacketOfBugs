package com.example.springreddit.controller;

import com.example.springreddit.dto.FilterDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class FilterController {

    @GetMapping("/filters")
    public ResponseEntity<Map<String, Object>> getFilters(){
        List<FilterDto> filters = Arrays.asList(
                new FilterDto(0L, "none", "Fără filtru"),
                new FilterDto(1L, "grayscale", "Alb-negru"),
                new FilterDto(2L, "sepia", "Sepia"),
                new FilterDto(3L, "invert", "Invertit")
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", filters);
        return ResponseEntity.ok(response);
    }
}
