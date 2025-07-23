package com.example.subtranslator.controller;

import com.example.subtranslator.service.ApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/translate")
public class TranslateController {

    private final ApiService apiService;

    public TranslateController(ApiService apiService) {
        this.apiService = apiService;
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> translateSrtContent(@RequestBody Map<String, String> request) {
        try {
            String fileContent = request.get("fileContent");
            String sourceLang = request.get("sourceLanguage");
            String targetLang = request.get("targetLanguage");
            String engine = request.get("engine");

            if (fileContent == null || fileContent.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Subtitle file content is missing"));
            }
            if (targetLang == null || targetLang.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Target language is missing"));
            }
            if (engine == null || engine.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Translation engine is required (gemini, google, or libre)"));
            }

            engine = engine.toLowerCase();
            if (!engine.equals("gemini") && !engine.equals("google") && !engine.equals("libre")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid translation engine. Use 'gemini', 'google', or 'libre'."));
            }

            String translatedContent = apiService.translateSrtContent(fileContent, sourceLang, targetLang, engine);

            return ResponseEntity.ok(Map.of("translatedContent", translatedContent));

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", "Translation failed due to internal error"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}