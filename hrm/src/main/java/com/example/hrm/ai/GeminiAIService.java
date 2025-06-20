package com.example.hrm.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeminiAIService {

    @Autowired
    private WebClient geminiWebClient;

    @Value("${ai.gemini.api.key}")
    private String apiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${ai.gemini.temperature:0.8}")      // ← Tăng default
    private Double temperature;

    @Value("${ai.gemini.max.tokens:4096}")      // ← Tăng default
    private Integer maxTokens;

    @Value("${ai.gemini.top.p:0.9}")            // ← Thêm mới
    private Double topP;

    @Value("${ai.gemini.top.k:40}")             // ← Thêm mới
    private Integer topK;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateContent(String prompt) {
        try {
            Map<String, Object> requestBody = buildRequestBody(prompt);

            String response = geminiWebClient.post()
                    .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return extractContentFromResponse(response);

        } catch (Exception e) {
            System.err.println("Gemini AI Error: " + e.getMessage());
            throw new RuntimeException("Gemini AI request failed: " + e.getMessage(), e);
        }
    }

    public Mono<String> generateContentAsync(String prompt) {
        Map<String, Object> requestBody = buildRequestBody(prompt);

        return geminiWebClient.post()
                .uri("/v1beta/models/{model}:generateContent?key={apiKey}", model, apiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::extractContentFromResponse)
                .onErrorMap(e -> new RuntimeException("Gemini AI async request failed: " + e.getMessage(), e));
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(Map.of("text", prompt)));

        // ✅ Cấu hình chi tiết hơn để AI trả lời như Gemini thật
        Map<String, Object> generationConfig = new HashMap<>();
        generationConfig.put("temperature", temperature);
        generationConfig.put("maxOutputTokens", maxTokens);
        generationConfig.put("topP", topP);                    // ← Thêm
        generationConfig.put("topK", topK);                    // ← Thêm
        generationConfig.put("candidateCount", 1);             // ← Thêm
        generationConfig.put("stopSequences", List.of());      // ← Thêm

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));
        requestBody.put("generationConfig", generationConfig);

        return requestBody;
    }

    private String extractContentFromResponse(String response) {
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.path("candidates")
                    .get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();
        } catch (Exception e) {
            System.err.println("Failed to parse Gemini response: " + e.getMessage());
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }

    public <T> T generateContentForObject(String prompt, Class<T> responseType) {
        String response = generateContent(prompt + "\n\nVui lòng trả về kết quả dưới dạng JSON hợp lệ.");
        try {
            String cleanedResponse = cleanJsonResponse(response);
            return objectMapper.readValue(cleanedResponse, responseType);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini JSON response: " + e.getMessage(), e);
        }
    }

    private String cleanJsonResponse(String response) {
        String cleaned = response.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        }
        if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }
}