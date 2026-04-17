package com.example.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import com.example.demo.dto.VectorEntry;

@Service
public class VectorSearchService {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private SimilarityService similarityService;

    public List<VectorEntry> search(List<Float> queryEmbedding) {

        return vectorStore.getAll().stream()
                .sorted((a, b) -> Double.compare(
                        similarityService.cosineSimilarity(b.getEmbedding(), queryEmbedding),
                        similarityService.cosineSimilarity(a.getEmbedding(), queryEmbedding)
                ))
                .limit(3) // Top K
                .toList();
    }
}
