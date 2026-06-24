package com.fitness.controller;

import com.fitness.dto.ApiResponse;
import com.fitness.memory.ChatHistory;
import com.fitness.security.SecurityUser;
import com.fitness.service.AiChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    private AiChatService aiChatService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(
            @AuthenticationPrincipal SecurityUser securityUser,
            @RequestParam String message,
            @RequestParam(defaultValue = "default") String sessionId) {
        return aiChatService.chat(securityUser.getUser().getId(), sessionId, message);
    }

    @GetMapping("/history")
    public ApiResponse<List<ChatHistory>> getHistory(
            @AuthenticationPrincipal SecurityUser securityUser) {
        return ApiResponse.success(aiChatService.getHistory(securityUser.getUser().getId()));
    }
}
