package com.example.demo.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.model.ErrorKnowledge;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

public class KnowledgeBase {

    public static List<ErrorKnowledge> getKnowledge() {

        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("errors.json").getInputStream();

            return mapper.readValue(inputStream, new TypeReference<List<ErrorKnowledge>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load knowledge base", e);
        }
    }
}
