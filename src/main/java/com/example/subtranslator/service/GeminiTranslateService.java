package com.example.subtranslator.service;

import com.google.gson.*;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
public class GeminiTranslateService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent";

    private final OkHttpClient client;

    public GeminiTranslateService() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(90, TimeUnit.SECONDS)
                .writeTimeout(90, TimeUnit.SECONDS)
                .build();
    }

    public String translate(String srtContent, String sourceLang, String targetLang) throws IOException, InterruptedException {

        List<String> chunks = splitSrtIntoChunks(srtContent, 40);
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<String>> futures = new ArrayList<>();

        for (String chunk : chunks) {
            String prompt = String.format(
                    "Translate the following subtitles into %s. " +
                    "Return ONLY the translated subtitles in valid SRT format. " +
                    "Do NOT include any explanations, options, or extra text. " +
                    "Preserve subtitle numbering and timestamps exactly.\n\n%s",
                    mapLanguageCode(targetLang),
                    chunk
            );

            futures.add(executor.submit(() -> {
                try {
                    return callGeminiAPI(prompt);
                } catch (IOException | InterruptedException e) {
                    return "";
                }
            }));
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);

        StringBuilder finalTranslated = new StringBuilder();
        for (Future<String> future : futures) {
            try {
                finalTranslated.append(future.get()).append("\n\n");
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        return finalTranslated.toString().trim();
    }

    private String callGeminiAPI(String promptText) throws IOException, InterruptedException {
        String fullUrl = GEMINI_URL + "?key=" + geminiApiKey;

        JsonObject textPart = new JsonObject();
        textPart.addProperty("text", promptText);

        JsonArray parts = new JsonArray();
        parts.add(textPart);

        JsonObject contentItem = new JsonObject();
        contentItem.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(contentItem);

        JsonObject json = new JsonObject();
        json.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("temperature", 0.5);
        generationConfig.addProperty("topP", 0.95);
        generationConfig.addProperty("topK", 40);
        json.add("generationConfig", generationConfig);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(fullUrl)
                .post(body)
                .build();

        int maxRetries = 5;
        int retryCount = 0;
        long waitTimeMs = 1000;

        while (true) {
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "No body";

                    if (response.code() == 503 && retryCount < maxRetries) {
                        retryCount++;
                        Thread.sleep(waitTimeMs);
                        waitTimeMs *= 2;
                        continue;
                    }

                    throw new IOException("Gemini API call failed: " + response.code() + " - " + errorBody);
                }

                String responseBody = response.body().string();
                JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

                return jsonResponse
                        .getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString();

            } catch (java.net.SocketTimeoutException e) {
                if (retryCount < maxRetries) {
                    retryCount++;
                    Thread.sleep(waitTimeMs);
                    waitTimeMs *= 2;
                } else {
                    throw new IOException("Gemini API call timed out after retries", e);
                }
            }
        }
    }

    private List<String> splitSrtIntoChunks(String srtContent, int chunkSize) {
        String[] entries = srtContent.trim().split("\\r?\\n\\r?\\n");
        List<String> chunks = new ArrayList<>();
        StringBuilder chunk = new StringBuilder();
        int count = 0;

        for (String entry : entries) {
            chunk.append(entry).append("\n\n");
            count++;
            if (count >= chunkSize) {
                chunks.add(chunk.toString());
                chunk = new StringBuilder();
                count = 0;
            }
        }

        if (chunk.length() > 0) {
            chunks.add(chunk.toString());
        }

        return chunks;
    }

    private String mapLanguageCode(String code) {
        return switch (code.toLowerCase()) {
            case "en" -> "English";
            case "si" -> "Sinhala";
            case "es" -> "Spanish";
            case "fr" -> "French";
            case "de" -> "German";
            case "it" -> "Italian";
            case "pt" -> "Portuguese";
            case "ru" -> "Russian";
            case "zh" -> "Chinese";
            case "ja" -> "Japanese";
            case "ko" -> "Korean";
            case "auto" -> "the appropriate language";
            default -> code;
        };
    }

    public String chat(String prompt) throws IOException, InterruptedException {
        return callGeminiAPI(prompt);
    }
}
