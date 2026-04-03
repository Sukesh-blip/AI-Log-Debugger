package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.model.ErrorKnowledge;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.demo.util.HashUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LogAnalysisService {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private CacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AnalysisResponse analyzeLog(String logMessage) {

        AnalysisResponse response = new AnalysisResponse();

        if (logMessage == null || logMessage.isEmpty()) {
            response.setRootCause("Invalid input");
            response.setExplanation("Log is empty");
            response.setSuggestedFix("Provide valid log");
            response.setConfidence(0.0);
            return response;
        }

        String key = HashUtil.sha256(logMessage);
        String cached = cacheService.get(key);

        if (cached != null) {
            log.info("Cache HIT");
            System.out.println("🔥 Cache HIT");
            try {
                return objectMapper.readValue(cached, AnalysisResponse.class);
            } catch (Exception e) {
                log.error("Error parsing cached response", e);
            }
        } else {
            log.info("Cache MISS");
        }

        Map<String, Integer> logVector = embeddingService.generateEmbedding(logMessage);

        List<ErrorKnowledge> knowledgeList = KnowledgeBase.getKnowledge();

        ErrorKnowledge bestMatch = null;
        double bestScore = 0;

        for (ErrorKnowledge knowledge : knowledgeList) {

            Map<String, Integer> knowledgeVector =
                    embeddingService.generateEmbedding(knowledge.getKeyword());

            double score = cosineSimilarity(logVector, knowledgeVector);

            if (score > bestScore) {
                bestScore = score;
                bestMatch = knowledge;
            }
        }

        if (bestMatch != null && bestScore > 0) {
            response.setRootCause(bestMatch.getRootCause());
            response.setExplanation(bestMatch.getExplanation());
            response.setSuggestedFix(bestMatch.getSolution());
            response.setConfidence(bestScore);
        } else {
            response.setRootCause("Unknown error");
            response.setExplanation("No match found");
            response.setSuggestedFix("Check manually");
            response.setConfidence(0.5);
        }

        try {
            cacheService.set(key, objectMapper.writeValueAsString(response), 21600); // 6 hours
        } catch (Exception e) {
            log.error("Error writing cache", e);
        }

        return response;
    }

    private double cosineSimilarity(Map<String, Integer> v1, Map<String, Integer> v2) {

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String key : v1.keySet()) {
            if (v2.containsKey(key)) {
                dotProduct += v1.get(key) * v2.get(key);
            }
            norm1 += Math.pow(v1.get(key), 2);
        }

        for (int value : v2.values()) {
            norm2 += Math.pow(value, 2);
        }

        if (norm1 == 0 || norm2 == 0) return 0;

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
