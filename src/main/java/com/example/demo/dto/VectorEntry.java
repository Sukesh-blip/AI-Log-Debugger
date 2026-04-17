package com.example.demo.dto;

import java.util.List;

public class VectorEntry {

    private String log;
    private List<Float> embedding;

    public VectorEntry(String log, List<Float> embedding) {
        this.log = log;
        this.embedding = embedding;
    }

    public String getLog() { return log; }
    public List<Float> getEmbedding() { return embedding; }
}
