package com.example.demo.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmbeddingService {

    public List<Float> generateEmbedding(String log) {
        // TEMP: mock embedding (we replace later with real model)
        
        List<Float> vector = new ArrayList<>();
        
        int hash = log.hashCode();
        
        for (int i = 0; i < 10; i++) {
            vector.add((float) ((hash + i) % 100) / 100);
        }

        return vector;
    }
}
