package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class LogAnalysisService {

    private static final Logger log = LoggerFactory.getLogger(LogAnalysisService.class);


    @Autowired
    private CacheService cacheService;

    @Autowired
    private RuleEngineService ruleEngineService;

    @Autowired
    private LLMService llmService;

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

        // 3️⃣ LLM (Groq)
        log.info("Calling Groq LLM...");
        String aiResponse = llmService.call(logMessage);

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

        // 4️⃣ Store in cache
        try {
            cacheService.set(key, aiResponse, 21600);
        } catch (Exception e) {
            log.error("Error writing cache", e);
        }

        // 5️⃣ Return
        try {
            return objectMapper.readValue(aiResponse, AnalysisResponse.class);
        } catch (Exception e) {
            log.error("Error parsing final LLM logic into API obj", e);
            return new AnalysisResponse("Parsing Error", "LLM format error", "Retry request", 0.0);
        }
    }
}
