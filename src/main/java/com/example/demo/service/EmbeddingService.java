package com.example.demo.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmbeddingService {

    public Map<String, Integer> generateEmbedding(String text) {

        Map<String, Integer> vector = new HashMap<>();

        String[] words = text.toLowerCase().split(" ");

        for (String word : words) {
            vector.put(word, vector.getOrDefault(word, 0) + 1);
        }

        return vector;
    }
}
