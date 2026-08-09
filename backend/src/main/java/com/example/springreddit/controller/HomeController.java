package com.example.springreddit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "𝗣𝗮𝗰𝗸𝗲𝘁 𝗼𝗳 𝗕𝘂𝗴𝘀";
    }
}