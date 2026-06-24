package com.fitness.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class LLMConfig {

    @Value("${langchain4j.openai.chat-model.base-url}")
    private String chatBaseUrl;

    @Value("${langchain4j.openai.chat-model.api-key}")
    private String chatApiKey;

    @Value("${langchain4j.openai.chat-model.model-name}")
    private String chatModelName;

    @Value("${langchain4j.openai.embedding-model.base-url}")
    private String embeddingBaseUrl;

    @Value("${langchain4j.openai.embedding-model.api-key}")
    private String embeddingApiKey;

    @Value("${langchain4j.openai.embedding-model.model-name}")
    private String embeddingModelName;

    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .baseUrl(chatBaseUrl)
                .apiKey(chatApiKey)
                .modelName(chatModelName)
                .timeout(Duration.ofSeconds(60))
                .build();
    }

    @Bean
    public EmbeddingModel embeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .baseUrl(embeddingBaseUrl)
                .apiKey(embeddingApiKey)
                .modelName(embeddingModelName)
                .build();
    }
}
