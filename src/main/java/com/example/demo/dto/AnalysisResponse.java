package com.example.demo.dto;

public class AnalysisResponse {

    private String rootCause;
    private String explanation;
    private String suggestedFix;
    private double confidence;

    public AnalysisResponse() {}

    public AnalysisResponse(String rootCause, String explanation, String suggestedFix, double confidence) {
        this.rootCause = rootCause;
        this.explanation = explanation;
        this.suggestedFix = suggestedFix;
        this.confidence = confidence;
    }

    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}