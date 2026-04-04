package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LLMService {

    private static final Logger log = LoggerFactory.getLogger(LLMService.class);

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:openai/gpt-oss-120b}")
    private String model;

    public String call(String logMessage) {
        log.info("🚀 Calling Groq API... (Model: {})", model);
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            String url = "https://api.groq.com/openai/v1/chat/completions";

            String prompt = """
                You are an expert backend debugging assistant.

                Analyze the given log and return ONLY a valid JSON response.

                Format strictly as:
                {
                  "rootCause": "...",
                  "explanation": "...",
                  "suggestedFix": "...",
                  "confidence": 0.0 to 1.0
                }

                Rules:
                - Do NOT return anything outside JSON
                - Do NOT add extra text
                - Keep response concise and technical

                Log:
                """ + logMessage;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            String body = """
                {
                    "model": "%s",
                    "messages": [{"role": "user", "content": "%s"}]
                }
                """.formatted(model, prompt.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r"));

            HttpEntity<String> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            
            log.info("✅ Groq Response received");
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response.getBody());
            String content = rootNode.path("choices").get(0).path("message").path("content").asText();
            
            if (!content.trim().startsWith("{")) {
                log.error("Invalid JSON from LLM");
            
                return """
                {
                  "rootCause": "Parsing error",
                  "explanation": "LLM returned unstructured response",
                  "suggestedFix": "Retry request",
                  "confidence": 0.3
                }
                """;
            }
            
            return content;
            
        } catch (Exception e) {
            log.error("❌ Groq API Error", e);
            return """
            {
              "rootCause": "API call failed",
              "explanation": "%s",
              "suggestedFix": "Check API status and keys",
              "confidence": 0.0
            }
            """.formatted(e.getMessage().replace("\"", "'"));
        }
    }
}
