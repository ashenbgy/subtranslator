package com.example.subtranslator.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class LibreTranslateService {

    @Value("${libretranslate.api.url}")
    private String libreTranslateUrl; // e.g. http://localhost:5000/translate

    private final OkHttpClient client = new OkHttpClient();

    public String translate(String srtContent, String sourceLang, String targetLang) throws IOException {
        String[] entries = srtContent.trim().split("\\r?\\n\\r?\\n");
        StringBuilder translatedContent = new StringBuilder();

        for (String entry : entries) {
            String[] lines = entry.split("\\r?\\n");
            if (lines.length >= 3) {
                String number = lines[0];
                String time = lines[1];
                StringBuilder text = new StringBuilder();
                for (int i = 2; i < lines.length; i++) {
                    text.append(lines[i]).append(" ");
                }

                String translatedText = libreTranslate(text.toString(), sourceLang, targetLang);

                translatedContent.append(String.format("%s\n%s\n%s\n\n", number, time, translatedText.trim()));
            } else {
                translatedContent.append(entry).append("\n\n");
            }
        }

        return translatedContent.toString().trim();
    }

    private String libreTranslate(String text, String sourceLang, String targetLang) throws IOException {
        JsonObject json = new JsonObject();
        json.addProperty("q", text);
        json.addProperty("source", sourceLang);
        json.addProperty("target", targetLang);
        json.addProperty("format", "text");

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));

        Request request = new Request.Builder()
                .url(libreTranslateUrl)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("LibreTranslate failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

            return jsonResponse.get("translatedText").getAsString();
        }
    }
}
