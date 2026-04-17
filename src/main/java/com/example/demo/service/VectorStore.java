package com.example.demo.service;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import com.example.demo.dto.VectorEntry;

@Component
public class VectorStore {

    private final List<VectorEntry> store = new ArrayList<>();

    public void save(String log, List<Float> embedding) {
        store.add(new VectorEntry(log, embedding));
    }

    public List<VectorEntry> getAll() {
        return store;
    }
}
