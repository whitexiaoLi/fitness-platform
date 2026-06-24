package com.fitness.rag;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EmbeddingService {

    @Autowired
    private EmbeddingModel embeddingModel;

    public float[] embed(String text) {
        Embedding embedding = embeddingModel.embed(text).content();
        float[] result = new float[embedding.vector().length];
        for (int i = 0; i < embedding.vector().length; i++) {
            result[i] = embedding.vector()[i];
        }
        return result;
    }

    public List<float[]> embedAll(List<String> texts) {
        return texts.stream().map(this::embed).toList();
    }
}
