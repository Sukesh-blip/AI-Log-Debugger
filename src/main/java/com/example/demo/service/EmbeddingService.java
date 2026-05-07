package com.example.demo.service;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.EmbeddingsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    @Autowired
    private OpenAIClient openAIClient;

    @Value("${azure.openai.embedding-deployment}")
    private String embeddingDeployment;

    public List<Float> generateEmbedding(String text) {
        log.info("Generating embedding for: [{}]",
                text.length() > 80 ? text.substring(0, 80) + "..." : text);

        EmbeddingsOptions options = new EmbeddingsOptions(List.of(text));

        EmbeddingItem item = openAIClient
                .getEmbeddings(embeddingDeployment, options)
                .getData()
                .get(0);

        return item.getEmbedding()
                .stream()
                .map(Number::floatValue)
                .collect(Collectors.toList());
    }
}