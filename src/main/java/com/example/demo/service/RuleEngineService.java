package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.model.ErrorKnowledge;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleEngineService {

    private static final Logger log = LoggerFactory.getLogger(RuleEngineService.class);

    @Autowired private EmbeddingService  embeddingService;
    @Autowired private SimilarityService similarityService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String checkRules(String logMessage) {
        try {
            // ── Fast keyword match (no embedding needed) ──────────────────
            if (logMessage.contains("connection refused")) {
                return objectMapper.writeValueAsString(new AnalysisResponse(
                        "Database connection failed",
                        "Connection refused — the target service is not running or unreachable.",
                        "Check DB service status, host, and port configuration.",
                        1.0
                ));
            }
            if (logMessage.contains("nullpointerexception")) {
                return objectMapper.writeValueAsString(new AnalysisResponse(
                        "Null object access",
                        "The application attempted to call a method on an uninitialized object.",
                        "Add null checks before usage. Use Optional or @NonNull annotations.",
                        1.0
                ));
            }
            if (logMessage.contains("outofmemoryerror")) {
                return objectMapper.writeValueAsString(new AnalysisResponse(
                        "JVM heap exhaustion",
                        "The application consumed all available heap memory.",
                        "Increase -Xmx heap size. Profile with VisualVM to find memory leaks.",
                        1.0
                ));
            }
            if (logMessage.contains("jwt") && logMessage.contains("signature")) {
                return objectMapper.writeValueAsString(new AnalysisResponse(
                        "JWT token signature mismatch",
                        "The JWT token was signed with a different secret than used to verify it.",
                        "Ensure the same secret key is used for signing and verification.",
                        1.0
                ));
            }

            // ── Semantic similarity match (requires embedding API) ────────
            // Wrapped separately so if embedding API fails, we just skip this
            // and fall through to the LLM instead of crashing
            try {
                List<Float> logVector = embeddingService.generateEmbedding(logMessage);
                List<ErrorKnowledge> knowledge = KnowledgeBase.getKnowledge();

                ErrorKnowledge bestMatch = null;
                double bestScore = 0;

                for (ErrorKnowledge entry : knowledge) {
                    List<Float> kbVector = embeddingService.generateEmbedding(
                            entry.getKeyword().toLowerCase()
                    );
                    double score = similarityService.cosineSimilarity(logVector, kbVector);
                    if (score > bestScore) {
                        bestScore = score;
                        bestMatch = entry;
                    }
                }

                if (bestMatch != null && bestScore > 0.80) {
                    log.info("Semantic rule match found (score={}): {}", bestScore, bestMatch.getRootCause());
                    return objectMapper.writeValueAsString(new AnalysisResponse(
                            bestMatch.getRootCause(),
                            bestMatch.getExplanation(),
                            bestMatch.getSolution(),
                            bestScore
                    ));
                }

            } catch (Exception embeddingEx) {
                log.warn("Semantic rule check skipped — embedding API unavailable: {}", embeddingEx.getMessage());
                // Fall through to LLM
            }

        } catch (Exception e) {
            log.warn("Rule engine failed entirely (non-fatal): {}", e.getMessage());
        }

        return null; // No rule matched — proceed to vector search + LLM
    }
}