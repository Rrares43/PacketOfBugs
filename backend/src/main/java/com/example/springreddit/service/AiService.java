package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Service
public class AiService {
    private final RestClient restClient = RestClient.create();
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @Value("${gemini.api.key}")
    private String apiKey;

    public String generateSummary(String title, String content){
        try{
            String prompt = "Summarize the following forum post in one single, short sentence. " +
                    "Do not use formatting. Title: " + title + "Content: " + content;

            Map<String, Object> requestBody = Map.of(
                    "contents",
                    List.of(Map.of("parts",
                            List.of(Map.of("text", prompt))
                    )
                    )
            );

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=" + apiKey;
            LOGGER.info("Generating AI summary with prompt: {}", prompt);

            JsonNode response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if(response != null && response.has("candidates")){
                LOGGER.info("AI summary generated successfully");
                return response.get("candidates").get(0)
                        .get("content").get("parts").get(0)
                        .get("text").asText().trim();
            }

            LOGGER.warn("AI summary not available");
            return "AI summary not available.";
        }
        catch (Exception e){
            LOGGER.error("Error generating summary: {}", e.getMessage());
            e.printStackTrace();
            return "Error generating summary";
        }
    }

    public String censorText(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }

        try {
            String prompt = "You are a strict text filter. Replace every profane, offensive, or slur word in the text below with asterisks (***), regardless of language. Keep all punctuation, spacing, and non-offensive words exactly as they are. Do not add any formatting, explanations, quotes, or extra text. Return STRICTLY only the filtered text and nothing else. Text: " + text;

            Map<String, Object> safetySettings = Map.of(
                    "category", "HARM_CATEGORY_HARASSMENT",
                    "threshold", "BLOCK_NONE"
            );

            Map<String, Object> requestBody = Map.of(
                    "contents",
                    List.of(Map.of("parts",
                            List.of(Map.of("text", prompt))
                    )),
                    "safetySettings",
                    List.of(
                            Map.of("category", "HARM_CATEGORY_HARASSMENT", "threshold", "BLOCK_NONE"),
                            Map.of("category", "HARM_CATEGORY_HATE_SPEECH", "threshold", "BLOCK_NONE"),
                            Map.of("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT", "threshold", "BLOCK_NONE"),
                            Map.of("category", "HARM_CATEGORY_DANGEROUS_CONTENT", "threshold", "BLOCK_NONE")
                    )
            );

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash-lite:generateContent?key=" + apiKey;
            LOGGER.info("Censoring text with AI filter");

            JsonNode response = restClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);

            if (response != null && response.has("candidates")) {
                String censoredText = response.get("candidates").get(0)
                        .get("content").get("parts").get(0)
                        .get("text").asText().trim();
                LOGGER.info("Text censored successfully");
                return censoredText;
            }

            LOGGER.warn("Text censorship returned empty response, returning original text");
            return text;
        } catch (Exception e) {
            LOGGER.error("Error censoring text: {}", e.getMessage());
            return text;
        }
    }

}
