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

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + apiKey;

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
            return "Error generating summary";
        }
    }

}
