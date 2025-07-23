package com.example.subtranslator.controller;

import com.example.subtranslator.dto.ChatRequest;
import com.example.subtranslator.dto.ChatResponse;
import com.example.subtranslator.service.ApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    @Autowired
    private ApiService apiService;

    @PostMapping
    public ChatResponse chat(@RequestBody ChatRequest request) throws Exception {
        String prompt = buildChatPrompt(request.getMessage());
        String reply = apiService.chatWithGemini(prompt);
        return new ChatResponse(reply);
    }

    private String buildChatPrompt(String userMessage) {
        return "You are a helpful assistant.\nUser: " + userMessage + "\nAssistant:";
    }
}