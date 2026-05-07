package com.example.demo.controller;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.dto.LogRequest;
import com.example.demo.service.LogAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
@Tag(name = "Log Analyzer", description = "AI-powered log analysis using Azure OpenAI + Azure AI Search")
public class LogController {

    @Autowired
    private LogAnalysisService logAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "Analyze a log message",
               description = "Returns root cause, explanation, fix, and confidence score.")
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody LogRequest request) {
        AnalysisResponse response = logAnalysisService.analyzeLog(request.getLog());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-ai")
    @Operation(summary = "Quick test", description = "Runs analysis on a hardcoded DB connection error.")
    public ResponseEntity<AnalysisResponse> testAI() {
        AnalysisResponse response = logAnalysisService.analyzeLog(
                "ERROR: connection refused while connecting to database at localhost:5432"
        );
        return ResponseEntity.ok(response);
    }
}