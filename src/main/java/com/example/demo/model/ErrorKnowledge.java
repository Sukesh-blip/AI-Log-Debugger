package com.example.demo.model;

public class ErrorKnowledge {

    private String keyword;
    private String rootCause;
    private String explanation;
    private String solution;

    public ErrorKnowledge() {} // REQUIRED for Jackson

    public ErrorKnowledge(String keyword, String rootCause, String explanation, String solution) {
        this.keyword = keyword;
        this.rootCause = rootCause;
        this.explanation = explanation;
        this.solution = solution;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getRootCause() {
        return rootCause;
    }

    public void setRootCause(String rootCause) {
        this.rootCause = rootCause;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSolution() {
        return solution;
    }

    public void setSolution(String solution) {
        this.solution = solution;
    }
}
