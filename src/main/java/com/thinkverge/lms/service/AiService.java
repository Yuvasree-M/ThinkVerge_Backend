package com.thinkverge.lms.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
@Service
public class AiService {

    private final ChatClient chatClient;

    // Constructor injection via Builder — Spring AI auto-configures this
    public AiService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public String getAiReply(String courseContext, String question) {
        try {
            return chatClient.prompt()
                .system("You are a helpful AI assistant for the course: " + courseContext)
                .user(question)
                .call()
                .content();
        } catch (RuntimeException e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("429") || msg.contains("quota")) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "AI is temporarily rate-limited. Please wait a moment and try again."
                );
            }
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "AI service is currently unavailable."
            );
        }
    }
}