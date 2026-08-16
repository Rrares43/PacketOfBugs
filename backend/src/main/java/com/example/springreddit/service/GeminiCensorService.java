package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class GeminiCensorService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=%s";

    private static final String PROMPT_TEMPLATE =
            "Censor all profane, vulgar, or offensive words in the following text by replacing each character of every profane word with an asterisk (*). "
                    + "Keep the rest of the text, punctuation, and spaces exactly as they are. "
                    + "Respond STRICTLY with only the censored text, with no explanations, introductions, or markdown formatting. Text: %s";

    private final RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    public GeminiCensorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String censor(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        try {
            String prompt = String.format(PROMPT_TEMPLATE, text);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.0)
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            String url = String.format(GEMINI_URL, apiKey);
            JsonNode response = restTemplate.postForObject(url, entity, JsonNode.class);

            if (response != null
                    && response.has("candidates")
                    && response.get("candidates").isArray()
                    && !response.get("candidates").isEmpty()) {
                JsonNode parts = response.get("candidates").get(0)
                        .path("content")
                        .path("parts");
                if (parts.isArray() && !parts.isEmpty()) {
                    String censored = parts.get(0).path("text").asText("").trim();
                    if (!censored.isEmpty()) {
                        return censored;
                    }
                }
            }

            LOGGER.warn("Gemini censor returned an empty response; saving original comment text");
            return text;
        } catch (Exception e) {
            LOGGER.warn("Gemini censor unavailable: {}; saving original comment text", e.getMessage());
            return text;
        }
    }
}
