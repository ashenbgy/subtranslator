package com.example.subtranslator.service;

import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ApiService {

    private final GoogleTranslateService googleTranslateService;
    private final GeminiTranslateService geminiTranslateService;
    private final LibreTranslateService libreTranslateService;

    public ApiService(GoogleTranslateService googleTranslateService,
                      GeminiTranslateService geminiTranslateService,
                      LibreTranslateService libreTranslateService) {
        this.googleTranslateService = googleTranslateService;
        this.geminiTranslateService = geminiTranslateService;
        this.libreTranslateService = libreTranslateService;
    }

    public String translateSrtContent(String srtContent, String sourceLang, String targetLang, String engine)
            throws IOException, InterruptedException {

        return switch (engine.toLowerCase()) {
            case "google" -> googleTranslateService.translate(srtContent, sourceLang, targetLang);
            case "gemini" -> geminiTranslateService.translate(srtContent, sourceLang, targetLang);
            case "libre" -> libreTranslateService.translate(srtContent, sourceLang, targetLang);
            default -> throw new IllegalArgumentException("Unsupported translation engine: " + engine);
        };
    }

    public String chatWithGemini(String prompt) throws IOException, InterruptedException {
        return geminiTranslateService.chat(prompt);
    }
}