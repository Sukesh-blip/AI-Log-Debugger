package com.example.demo.service;

import com.example.demo.dto.AnalysisResponse;
import org.springframework.stereotype.Service;

@Service
public class LogAnalysisService {

    public AnalysisResponse analyzeLog(String log) {

        // TEMPORARY MOCK LOGIC
        return AnalysisResponse.builder()
                .rootCause("Database schema mismatch")
                .explanation("Column 'updated_at' not found in table")
                .suggestedFix("Add column using ALTER TABLE")
                .confidence(0.85)
                .build();
    }
}
