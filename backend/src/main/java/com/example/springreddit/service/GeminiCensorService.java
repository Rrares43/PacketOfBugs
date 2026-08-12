package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiCensorService {

    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    private static final String PROMPT_TEMPLATE =
            "Censor all profane, vulgar, or offensive words in the following text by replacing each character of every profane word with an asterisk (*). Keep the rest of the text, punctuation, and spaces exactly as they are. Respond STRICTLY with only the censored text, with no explanations, introductions, or markdown formatting. Text: %s";

    private final RestTemplate restTemplate;

    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    public GeminiCensorService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String censor(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;
            String prompt = String.format(PROMPT_TEMPLATE, input);
            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(
                                    Map.of("text", prompt)
                            ))
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

            String censoredText = extractCensoredText(response);
            return censoredText != null ? censoredText.trim() : input;
        } catch (Exception e) {
            LOGGER.warn("Gemini censor API unavailable, returning original text: {}", e.getMessage());
            return input;
        }
    }

    @SuppressWarnings("unchecked")
    private String extractCensoredText(Map<String, Object> response) {
        if (response == null) {
            return null;
        }

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return null;
        }

        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) {
            return null;
        }

        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts == null || parts.isEmpty()) {
            return null;
        }

        Object text = parts.get(0).get("text");
        return text != null ? text.toString() : null;
    }
}
