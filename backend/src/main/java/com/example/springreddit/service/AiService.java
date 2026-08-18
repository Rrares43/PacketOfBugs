package com.example.springreddit.service;

import com.example.springreddit.logging.CustomLogger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AiService {
    private final RestClient restClient = RestClient.create();
    private static final CustomLogger LOGGER = CustomLogger.getInstance();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Async
    public CompletableFuture<String> generateSummary(String title, String content){
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
                return CompletableFuture.completedFuture(response.get("candidates").get(0)
                        .get("content").get("parts").get(0)
                        .get("text").asText().trim());
            }

            LOGGER.warn("AI summary not available");
            return CompletableFuture.completedFuture("AI summary not available.");
        }
        catch (Exception e){
            LOGGER.error("Error generating summary: {}", e.getMessage());
            return CompletableFuture.completedFuture("Error generating summary");
        }
    }

    @Async
    public CompletableFuture<String> censorText(String text) {
        if (text == null || text.isBlank()) {
            return CompletableFuture.completedFuture(text);
        }

        try {
            String prompt = """
                    You are an advanced multilingual content moderation engine.
                    Replace EVERY profanity, slur, insult, vulgarity, or sexually explicit term in ANY language with asterisks (*).
                    
                    STRICT RULES:
                    1. EXACT CHARACTER MATCHING: For every censored word, replace each character with exactly ONE asterisk (*) so the number of asterisks matches the exact length of the original word (e.g., a 4-letter word becomes '****', a 5-letter word becomes '*****', 'muie' becomes '****', 'fuck' becomes '****', 'asshole' becomes '*******').
                    2. Detect and censor obfuscated words and LEETSPEAK (e.g. '@$$hole' -> '*******', 'm!erd@' -> '******', 'p3nd3j0' -> '*******', 'c@zz0' -> '*****').
                    3. Detect and censor spaced-out bad words (e.g. 'f u c k' -> '*******' or '****', 'm.u.i.e' -> '*******').
                    4. Detect and censor elongated bad words (e.g. 'c@caaaat' -> '********').
                    5. Keep all clean words, legitimate punctuation, and casing intact.
                    6. Return STRICTLY the processed text. Do NOT add notes, explanations, quotes, or markdown.
                    
                    Text: """ + text;

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
                return CompletableFuture.completedFuture(censoredText);
            }

            LOGGER.warn("Text censorship returned empty response, returning original text");
            return CompletableFuture.completedFuture(text);
        } catch (Exception e) {
            LOGGER.error("Error censoring text: {}", e.getMessage());
            return CompletableFuture.completedFuture(text);
        }
    }

}
