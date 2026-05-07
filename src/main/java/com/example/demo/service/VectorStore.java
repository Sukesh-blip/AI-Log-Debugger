package com.example.demo.service;

import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchDocument;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.VectorizedQuery;
import com.azure.search.documents.models.VectorSearchOptions;
import com.example.demo.dto.VectorEntry;
import com.example.demo.model.LogSearchDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class VectorStore {

    private static final Logger log = LoggerFactory.getLogger(VectorStore.class);
    private static final int    TOP_K                = 3;
    private static final double SIMILARITY_THRESHOLD = 0.82;

    @Autowired
    private SearchClient searchClient;

    public void save(String logMessage, List<Float> embedding) {
        try {
            // Use a plain Map instead of typed class — avoids constructor/annotation issues
            Map<String, Object> doc = new HashMap<>();
            doc.put("id", UUID.randomUUID().toString());
            doc.put("log", logMessage);
            doc.put("embedding", embedding);

            searchClient.uploadDocuments(List.of(doc));
            log.info("Saved log to Azure AI Search.");
        } catch (Exception e) {
            log.error("Failed to save to Azure AI Search: {}", e.getMessage());
        }
    }

    public List<VectorEntry> search(List<Float> queryEmbedding) {
        try {
            VectorizedQuery vectorQuery = new VectorizedQuery(queryEmbedding)
                    .setFields("embedding")
                    .setKNearestNeighborsCount(TOP_K);

            SearchOptions options = new SearchOptions()
                    .setVectorSearchOptions(
                            new VectorSearchOptions().setQueries(vectorQuery)
                    )
                    .setSelect("id", "log")
                    .setTop(TOP_K);

            List<VectorEntry> results = new ArrayList<>();

            searchClient.search(null, options, null).forEach(result -> {
                double score = result.getScore();

                if (score >= SIMILARITY_THRESHOLD) {
                    // Use SearchDocument (Map-based) — safe, no typed deserialization needed
                    SearchDocument doc = result.getDocument(SearchDocument.class);
                    Object logField = doc.get("log");

                    if (logField != null) {
                        String logText = logField.toString();
                        results.add(new VectorEntry(logText, List.of()));
                        log.info("Vector match accepted (score={}): {}",
                                String.format("%.3f", score),
                                logText.length() > 60 ? logText.substring(0, 60) + "..." : logText);
                    }
                } else {
                    log.info("Vector match rejected — score {} below threshold",
                            String.format("%.3f", score));
                }
            });

            log.info("Vector search: {} match(es) above threshold.", results.size());
            return results;

        } catch (Exception e) {
            log.error("Azure AI Search query failed: {}", e.getMessage());
            return List.of();
        }
    }
}