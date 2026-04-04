package com.example.demo.controller;

import com.example.demo.dto.AnalysisResponse;
import com.example.demo.dto.LogRequest;
import com.example.demo.service.LogAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/logs")
public class LogController {



    @Autowired
    private LogAnalysisService logAnalysisService;

    @PostMapping("/analyze")
    public AnalysisResponse analyze(@RequestBody LogRequest request) {
        return logAnalysisService.analyzeLog(request.getLog());
    }

    @GetMapping("/test-ai")
    public AnalysisResponse testAI() {
        return logAnalysisService.analyzeLog("DB connection refused error");
    }
}
