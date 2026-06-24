package com.fitness.rag;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Simplified Pinecone integration.
 * In production, use Pinecone Java client or LangChain4j Pinecone module.
 * This provides a stub that can be replaced with the full implementation.
 */
@Service
public class PineconeStoreService {

    @Value("${pinecone.api-key}")
    private String apiKey;

    @Value("${pinecone.host}")
    private String host;

    @Value("${pinecone.index-name}")
    private String indexName;

    @Autowired
    private EmbeddingService embeddingService;

    // In-memory store for MVP (replace with actual Pinecone client)
    private final List<KnowledgeEntry> store = new ArrayList<>();

    @PostConstruct
    public void init() {
        // Load knowledge base from resources
        // In production, this would upsert to Pinecone index
    }

    public void addKnowledge(String text, Map<String, String> metadata) {
        float[] vector = embeddingService.embed(text);
        store.add(new KnowledgeEntry(text, vector, metadata));
    }

    public List<String> search(String query, int topK) {
        float[] queryVector = embeddingService.embed(query);
        return store.stream()
                .sorted((a, b) -> Double.compare(
                        cosineSimilarity(b.vector, queryVector),
                        cosineSimilarity(a.vector, queryVector)))
                .limit(topK)
                .map(e -> e.text)
                .toList();
    }

    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private record KnowledgeEntry(String text, float[] vector, Map<String, String> metadata) {}
}
