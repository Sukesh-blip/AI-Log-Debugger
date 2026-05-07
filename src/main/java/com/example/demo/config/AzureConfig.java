package com.example.demo.config;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureConfig.class);

    @Value("${azure.openai.endpoint}")
    private String openAiEndpoint;

    @Value("${azure.openai.api-key}")
    private String openAiKey;

    @Bean
    public OpenAIClient openAIClient() {
        log.info("Building OpenAI client with endpoint: [{}]", openAiEndpoint);
        return new OpenAIClientBuilder()
                .endpoint(openAiEndpoint)
                .credential(new AzureKeyCredential(openAiKey))
                .buildClient();
    }

    @Value("${azure.search.endpoint}")
    private String searchEndpoint;

    @Value("${azure.search.api-key}")
    private String searchKey;

    @Value("${azure.search.index-name}")
    private String indexName;

    @Bean
    public SearchClient searchClient() {
        return new SearchClientBuilder()
                .endpoint(searchEndpoint)
                .credential(new AzureKeyCredential(searchKey))
                .indexName(indexName)
                .buildClient();
    }

    @Bean
    public SearchIndexClient searchIndexClient() {
        return new SearchIndexClientBuilder()
                .endpoint(searchEndpoint)
                .credential(new AzureKeyCredential(searchKey))
                .buildClient();
    }
}