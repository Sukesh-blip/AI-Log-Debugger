package com.example.demo.config;

import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.HnswAlgorithmConfiguration;
import com.azure.search.documents.indexes.models.HnswParameters;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchFieldDataType;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.indexes.models.VectorSearch;
import com.azure.search.documents.indexes.models.VectorSearchAlgorithmMetric;
import com.azure.search.documents.indexes.models.VectorSearchProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SearchIndexInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SearchIndexInitializer.class);

    private static final String VECTOR_PROFILE = "log-vector-profile";
    private static final String HNSW_CONFIG    = "log-hnsw-config";
    private static final int    EMBEDDING_DIMS = 1536;

    @Autowired
    private SearchIndexClient searchIndexClient;

    @Value("${azure.search.index-name}")
    private String indexName;

    @Override
    public void run(ApplicationArguments args) {
        try {
            searchIndexClient.getIndex(indexName);
            log.info("Index '{}' already exists — skipping creation.", indexName);
        } catch (Exception e) {
            log.info("Index '{}' not found — creating...", indexName);
            createIndex();
        }
    }

    private void createIndex() {
        // HNSW algorithm
        HnswAlgorithmConfiguration hnsw = new HnswAlgorithmConfiguration(HNSW_CONFIG)
                .setParameters(new HnswParameters()
                        .setM(4)
                        .setEfConstruction(400)
                        .setEfSearch(500)
                        .setMetric(VectorSearchAlgorithmMetric.COSINE));

        // Vector profile
        VectorSearchProfile profile = new VectorSearchProfile(VECTOR_PROFILE, HNSW_CONFIG);

        // VectorSearch config
        VectorSearch vectorSearch = new VectorSearch()
                .setAlgorithms(List.of(hnsw))
                .setProfiles(List.of(profile));

        // id field — key field
        SearchField idField = new SearchField("id", SearchFieldDataType.STRING);
        idField.setKey(true);

        // log field — searchable and retrievable
        // setHidden(false) = retrievable, setHidden(true) = NOT retrievable
        SearchField logField = new SearchField("log", SearchFieldDataType.STRING);
        logField.setSearchable(true);
        logField.setHidden(false);  // retrievable

        // embedding field — vector field, hidden (not returned in results)
        SearchField embeddingField = new SearchField(
                "embedding",
                SearchFieldDataType.collection(SearchFieldDataType.SINGLE)
        );
        embeddingField.setSearchable(true);
        embeddingField.setHidden(true);   // NOT retrievable — no need to return raw vectors
        embeddingField.setVectorSearchDimensions(EMBEDDING_DIMS);
        embeddingField.setVectorSearchProfileName(VECTOR_PROFILE);

        // Build and create
        SearchIndex index = new SearchIndex(indexName)
                .setFields(Arrays.asList(idField, logField, embeddingField))
                .setVectorSearch(vectorSearch);

        searchIndexClient.createIndex(index);
        log.info("Index '{}' created successfully.", indexName);
    }
}