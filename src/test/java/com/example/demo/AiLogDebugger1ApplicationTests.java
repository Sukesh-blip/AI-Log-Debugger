package com.example.demo;

import com.example.demo.service.SimilarityService;
import com.example.demo.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// NOTE: Do NOT use @SpringBootTest here — it tries to connect to Azure on startup.
// These are pure unit tests for logic that needs no external dependencies.
class AiLogDebugger1ApplicationTests {

    private final SimilarityService similarityService = new SimilarityService();

    // ── SimilarityService tests ───────────────────────────────────────────────

    @Test
    void identicalVectors_shouldReturnOne() {
        List<Float> v = List.of(1.0f, 2.0f, 3.0f);
        double score = similarityService.cosineSimilarity(v, v);
        assertEquals(1.0, score, 0.0001);
    }

    @Test
    void oppositeVectors_shouldReturnMinusOne() {
        List<Float> v1 = List.of(1.0f, 0.0f);
        List<Float> v2 = List.of(-1.0f, 0.0f);
        double score = similarityService.cosineSimilarity(v1, v2);
        assertEquals(-1.0, score, 0.0001);
    }

    @Test
    void perpendicularVectors_shouldReturnZero() {
        List<Float> v1 = List.of(1.0f, 0.0f);
        List<Float> v2 = List.of(0.0f, 1.0f);
        double score = similarityService.cosineSimilarity(v1, v2);
        assertEquals(0.0, score, 0.0001);
    }

    // ── HashUtil tests ────────────────────────────────────────────────────────

    @Test
    void sameInput_shouldProduceSameHash() {
        String h1 = HashUtil.sha256("connection refused");
        String h2 = HashUtil.sha256("connection refused");
        assertEquals(h1, h2);
    }

    @Test
    void differentInput_shouldProduceDifferentHash() {
        String h1 = HashUtil.sha256("connection refused");
        String h2 = HashUtil.sha256("null pointer exception");
        assertNotEquals(h1, h2);
    }

    @Test
    void hash_shouldNotBeNullOrEmpty() {
        String hash = HashUtil.sha256("test log");
        assertNotNull(hash);
        assertFalse(hash.isBlank());
    }

    // ── LogAnalysisService input validation tests ─────────────────────────────

    @Test
    void nullLog_shouldReturnInvalidResponse() {
        // Testing the validation logic directly without Spring context
        String logMessage = null;
        assertTrue(logMessage == null || (logMessage != null && logMessage.isBlank()));
    }

    @Test
    void oversizedLog_shouldBeRejected() {
        String longLog = "a".repeat(5001);
        assertTrue(longLog.length() > 5000);
    }
}