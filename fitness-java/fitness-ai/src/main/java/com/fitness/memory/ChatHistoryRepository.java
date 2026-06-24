package com.fitness.memory;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ChatHistoryRepository extends MongoRepository<ChatHistory, String> {
    Optional<ChatHistory> findByUserIdAndSessionId(Long userId, String sessionId);
    List<ChatHistory> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
}
