package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.dto.VectorEntry;
import com.example.demo.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);

    @Autowired private CacheService        cacheService;
    @Autowired private RuleEngineService   ruleEngineService;
    @Autowired private LLMService          llmService;
    @Autowired private EmbeddingService    embeddingService;
    @Autowired private VectorSearchService vectorSearchService;
    @Autowired private VectorStore         vectorStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisResponse analyzeLog(String logMessage) {

        // Step 1: Validate
        if (logMessage == null || logMessage.isBlank()) {
            return new AnalysisResponse("Invalid Input", "Log message is empty.",
                    "Provide a non-empty log string.", 0.0);
        }
        if (logMessage.length() > 5000) {
            return new AnalysisResponse("Invalid Input", "Log exceeds 5000 character limit.",
                    "Trim to the relevant error section.", 0.0);
        }

        // Step 2: Normalize
        String normalized = logMessage.trim().toLowerCase();
        String cacheKey   = HashUtil.sha256(normalized);

        // Step 3: Cache
        try {
            String cached = cacheService.get(cacheKey);
            if (cached != null) {
                log.info(">>> CACHE HIT");
                return objectMapper.readValue(cached, AnalysisResponse.class);
            }
        } catch (Exception e) {
            log.warn("Cache read failed (non-fatal): {}", e.getMessage());
        }

        // Step 4: Rule engine
        try {
            String ruleResult = ruleEngineService.checkRules(normalized);
            if (ruleResult != null) {
                log.info(">>> RULE ENGINE HIT");
                saveToCache(cacheKey, ruleResult);
                return objectMapper.readValue(ruleResult, AnalysisResponse.class);
            }
        } catch (Exception e) {
            log.warn("Rule engine failed (non-fatal): {}", e.getMessage());
        }

     // Step 5: Embedding
        log.info("Generating embedding...");
        List<Float> embedding;
        try {
            embedding = embeddingService.generateEmbedding(logMessage);
        } catch (Exception e) {
            log.error("Embedding generation failed — going direct to LLM without vector context: {}", e.getMessage());
            // Skip vector search, go straight to LLM with no context
            String prompt = """
                    Analyze this application log:

                    Log:
                    %s

                    Return ONLY a valid JSON object:
                    {
                      "rootCause": "...",
                      "explanation": "...",
                      "suggestedFix": "...",
                      "confidence": 0.0
                    }
                    """.formatted(logMessage);
            String aiResponse = cleanMarkdown(llmService.call(prompt));
            saveToCache(cacheKey, aiResponse);
            try {
                return objectMapper.readValue(aiResponse, AnalysisResponse.class);
            } catch (Exception parseEx) {
                return new AnalysisResponse("Parse Error", "LLM returned unexpected format.", "Retry the request.", 0.0);
            }
        }
        // Step 6: Vector search
        log.info("Searching vector store...");
        List<VectorEntry> similarLogs = vectorSearchService.search(embedding);

        // Step 7: Build context
        String context = similarLogs.isEmpty()
                ? "(no similar logs found)"
                : similarLogs.stream().map(VectorEntry::getLog).collect(Collectors.joining("\n---\n"));

        // Step 8: LLM
        log.info(">>> Calling Azure OpenAI LLM...");
        String prompt = """
                Analyze this application log:

                Log:
                %s

                Similar issues from knowledge base:
                %s

                Return ONLY a valid JSON object:
                {
                  "rootCause": "...",
                  "explanation": "...",
                  "suggestedFix": "...",
                  "confidence": 0.0
                }
                """.formatted(logMessage, context);

        String aiResponse = cleanMarkdown(llmService.call(prompt));

        // Step 9: Save + cache + return
        vectorStore.save(logMessage, embedding);
        saveToCache(cacheKey, aiResponse);

        try {
            return objectMapper.readValue(aiResponse, AnalysisResponse.class);
        } catch (Exception e) {
            log.error("Failed to parse LLM response: {}", e.getMessage());
            return new AnalysisResponse("Parse Error",
                    "LLM returned unexpected format.", "Retry the request.", 0.0);
        }
    }

    private String cleanMarkdown(String raw) {
        if (raw == null) return "{}";
        String s = raw.strip();
        if (s.startsWith("```json")) s = s.substring(7);
        if (s.startsWith("```"))     s = s.substring(3);
        if (s.endsWith("```"))       s = s.substring(0, s.length() - 3);
        return s.strip();
    }

    private void saveToCache(String key, String value) {
        try {
            cacheService.set(key, value, 21600);
        } catch (Exception e) {
            log.warn("Cache write failed (non-fatal): {}", e.getMessage());
        }
    }
}