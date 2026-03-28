package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.model.ErrorKnowledge;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Service
public class LogAnalysisService {

    @Autowired
    private EmbeddingService embeddingService;

    public AnalysisResponse analyzeLog(String log) {

        AnalysisResponse response = new AnalysisResponse();

        if (log == null || log.isEmpty()) {
            response.setRootCause("Invalid input");
            response.setExplanation("Log is empty");
            response.setSuggestedFix("Provide valid log");
            response.setConfidence(0.0);
            return response;
        }

        Map<String, Integer> logVector = embeddingService.generateEmbedding(log);

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
