package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.util.HashUtil;
import com.example.demo.dto.VectorEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);

    @Autowired
    private CacheService cacheService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private LLMService llmService;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorSearchService vectorSearchService;

    @Autowired
    private VectorStore vectorStore;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisResponse analyzeLog(String logMessage) {
        if (logMessage == null || logMessage.isEmpty()) {
            return new AnalysisResponse("Invalid input", "Log is empty", "Provide valid log", 0.0);
        }

        // 1. Normalize
        String normalized = logMessage.trim().toLowerCase();

        String key = HashUtil.sha256(normalized);

        // 1️⃣ Cache
        String cached = cacheService.get(key);
        if (cached != null) {
            log.info("Cache HIT");
            try { return objectMapper.readValue(cached, AnalysisResponse.class); } catch(Exception e) {}
        }

        // 2️⃣ Rule Engine
        String ruleResult = ruleEngineService.checkRules(normalized);
        if (ruleResult != null) {
            log.info("Rule Engine HIT");
            // Store Rule Engine result in cache too to optimize future identical requests
            try { cacheService.set(key, ruleResult, 21600); } catch(Exception e) {}
            try { return objectMapper.readValue(ruleResult, AnalysisResponse.class); } catch(Exception e) {}
        }

        // 3️⃣ Embedding Generation (RAG Step 1)
        List<Float> embedding = embeddingService.generateEmbedding(logMessage);

        // 4️⃣ Vector Search (RAG Step 2)
        List<VectorEntry> similarLogs = vectorSearchService.search(embedding);

        // 5️⃣ Context Builder (RAG Step 3)
        String context = similarLogs.stream()
                .map(VectorEntry::getLog)
                .collect(Collectors.joining("\n"));

        // 6️⃣ LLM (Groq) with Context
        log.info("Calling Groq LLM...");
        String prompt = "Log:\n" + logMessage +
                "\n\nSimilar Issues:\n" + context +
                "\n\nFind root cause and fix. Return strictly in standard JSON format expected.";
                
        String aiResponse = llmService.call(prompt);

        // Clean up markdown syntax from LLM output if any
        if (aiResponse.startsWith("```json")) {
            aiResponse = aiResponse.substring(7);
        }
        if (aiResponse.startsWith("```")) {
            aiResponse = aiResponse.substring(3);
        }
        if (aiResponse.endsWith("```")) {
            aiResponse = aiResponse.substring(0, aiResponse.length() - 3);
        }

        // 7️⃣ Store into Vector DB
        vectorStore.save(logMessage, embedding);

        // 8️⃣ Store in cache
        try {
            cacheService.set(key, aiResponse, 21600);
        } catch (Exception e) {
            log.error("Error writing cache", e);
        }

        // 9️⃣ Return
        try {
            return objectMapper.readValue(aiResponse, AnalysisResponse.class);
        } catch (Exception e) {
            log.error("Error parsing final LLM logic into API obj", e);
            return new AnalysisResponse("Parsing Error", "LLM format error", "Retry request", 0.0);
        }
    }
}
