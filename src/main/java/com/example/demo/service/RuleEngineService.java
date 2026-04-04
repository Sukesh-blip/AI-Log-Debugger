package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.model.ErrorKnowledge;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class RuleEngineService {

    @Autowired
    private EmbeddingService embeddingService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String checkRules(String logMessage) {
        try {
            // Simple regex / keyword matching first (Fast Rule processing)
            String normalizedLog = logMessage.toLowerCase();
            if (normalizedLog.contains("connection refused")) {
                AnalysisResponse response = new AnalysisResponse(
                    "Database connection failed",
                    "Connection refused indicates service is not running",
                    "Check DB service",
                    1.0
                );
                return objectMapper.writeValueAsString(response);
            }

            // Semantic embedding similarity matching (Advanced Rule processing)
            Map<String, Integer> logVector = embeddingService.generateEmbedding(normalizedLog);
            List<ErrorKnowledge> knowledgeList = KnowledgeBase.getKnowledge();

            ErrorKnowledge bestMatch = null;
            double bestScore = 0;

            for (ErrorKnowledge knowledge : knowledgeList) {
                Map<String, Integer> knowledgeVector = embeddingService.generateEmbedding(knowledge.getKeyword().toLowerCase());
                double score = cosineSimilarity(logVector, knowledgeVector);

                if (score > bestScore) {
                    bestScore = score;
                    bestMatch = knowledge;
                }
            }

            if (bestMatch != null && bestScore > 0.8) {
                AnalysisResponse response = new AnalysisResponse(
                    bestMatch.getRootCause(),
                    bestMatch.getExplanation(),
                    bestMatch.getSolution(),
                    bestScore
                );
                return objectMapper.writeValueAsString(response);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
