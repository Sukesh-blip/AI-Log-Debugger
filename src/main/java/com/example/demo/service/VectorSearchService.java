package com.example.demo.service;

import com.example.demo.dto.VectorEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorSearchService {

    @Autowired
    private VectorStore vectorStore;

    public List<VectorEntry> search(List<Float> queryEmbedding) {
        return vectorStore.search(queryEmbedding);
    }
}