package com.example.demo.dto;

public class AnalysisResponse {
    private String rootCause;
    private String explanation;
    private String suggestedFix;
    private double confidence;

    // Constructors
    public AnalysisResponse() {}

    public AnalysisResponse(String rootCause, String explanation, String suggestedFix, double confidence) {
        this.rootCause = rootCause;
        this.explanation = explanation;
        this.suggestedFix = suggestedFix;
        this.confidence = confidence;
    }

    // Getters and Setters
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public String getSuggestedFix() { return suggestedFix; }
    public void setSuggestedFix(String suggestedFix) { this.suggestedFix = suggestedFix; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    // Builder Pattern
    public static AnalysisResponseBuilder builder() {
        return new AnalysisResponseBuilder();
    }

    public static class AnalysisResponseBuilder {
        private String rootCause;
        private String explanation;
        private String suggestedFix;
        private double confidence;

        public AnalysisResponseBuilder rootCause(String rootCause) {
            this.rootCause = rootCause;
            return this;
        }

        public AnalysisResponseBuilder explanation(String explanation) {
            this.explanation = explanation;
            return this;
        }

        public AnalysisResponseBuilder suggestedFix(String suggestedFix) {
            this.suggestedFix = suggestedFix;
            return this;
        }

        public AnalysisResponseBuilder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public AnalysisResponse build() {
            return new AnalysisResponse(rootCause, explanation, suggestedFix, confidence);
        }
    }
}
