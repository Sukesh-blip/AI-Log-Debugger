package com.example.demo.service;

import com.example.demo.model.ErrorKnowledge;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.List;

public class KnowledgeBase {

    public static List<ErrorKnowledge> getKnowledge() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource("errors.json").getInputStream();
            return mapper.readValue(is, new TypeReference<List<ErrorKnowledge>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Failed to load knowledge base", e);
        }
    }
}