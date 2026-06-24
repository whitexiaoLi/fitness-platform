package com.fitness.service;

import com.fitness.memory.ChatHistory;
import com.fitness.memory.ChatHistoryRepository;
import com.fitness.memory.ChatMessage;
import com.fitness.rag.PineconeStoreService;
import com.fitness.tool.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.TokenStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class AiChatService {

    @Autowired
    private ChatLanguageModel chatLanguageModel;

    @Autowired
    private ChatHistoryRepository chatHistoryRepository;

    @Autowired
    private PineconeStoreService pineconeStore;

    @Autowired
    private TrainingTool trainingTool;

    @Autowired
    private BodyMetricsTool bodyMetricsTool;

    @Autowired
    private DietTool dietTool;

    @Autowired
    private CourseTool courseTool;

    @Autowired
    private PlanTool planTool;

    /**
     * Chat interface for LangChain4j AiServices.
     */
    interface Assistant {
        TokenStream chat(String userMessage);
    }

    public Flux<String> chat(Long userId, String sessionId, String userMessage) {
        // 1. Load or create chat history
        ChatHistory history = chatHistoryRepository
                .findByUserIdAndSessionId(userId, sessionId)
                .orElseGet(() -> {
                    ChatHistory h = new ChatHistory();
                    h.setUserId(userId);
                    h.setSessionId(sessionId);
                    h.setMessages(new ArrayList<>());
                    h.setCreatedAt(LocalDateTime.now());
                    return h;
                });

        // 2. Build chat memory from history
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.withMaxMessages(20);

        // 3. Build RAG context from knowledge base
        String knowledgeContext = String.join("\n",
                pineconeStore.search(userMessage, 3));

        // 4. Build system prompt with tools and knowledge
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(chatMemory)
                .tools(trainingTool, bodyMetricsTool, dietTool, courseTool, planTool)
                .build();

        // 5. Stream response
        TokenStream tokenStream = assistant.chat(userMessage);

        // 6. Save history after response
        StringBuilder responseBuilder = new StringBuilder();
        return Flux.create(sink -> {
            tokenStream.onNext(token -> {
                responseBuilder.append(token);
                sink.next(token);
            });
            tokenStream.onComplete(response -> {
                // Save user message
                history.getMessages().add(new ChatMessage("USER", userMessage, LocalDateTime.now()));
                // Save assistant response
                history.getMessages().add(new ChatMessage("ASSISTANT",
                        responseBuilder.toString(), LocalDateTime.now()));
                history.setUpdatedAt(LocalDateTime.now());
                chatHistoryRepository.save(history);
                sink.complete();
            });
            tokenStream.onError(sink::error);
            tokenStream.start();
        });
    }

    public java.util.List<ChatHistory> getHistory(Long userId) {
        return chatHistoryRepository.findByUserIdOrderByUpdatedAtDesc(userId,
                org.springframework.data.domain.PageRequest.of(0, 20));
    }
}
