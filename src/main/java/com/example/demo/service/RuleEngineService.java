package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.model.ErrorKnowledge;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RuleEngineService {

    @Autowired
    private EmbeddingService embeddingService;
    
    @Autowired
    private SimilarityService similarityService;

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
            List<Float> logVector = embeddingService.generateEmbedding(normalizedLog);
            List<ErrorKnowledge> knowledgeList = KnowledgeBase.getKnowledge();

            ErrorKnowledge bestMatch = null;
            double bestScore = 0;

            for (ErrorKnowledge knowledge : knowledgeList) {
                List<Float> knowledgeVector = embeddingService.generateEmbedding(knowledge.getKeyword().toLowerCase());
                double score = similarityService.cosineSimilarity(logVector, knowledgeVector);

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
}
