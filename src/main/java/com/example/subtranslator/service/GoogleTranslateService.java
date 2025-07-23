package com.example.subtranslator.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GoogleTranslateService {

    @Value("${google.api.key}")
    private String googleApiKey;

    private static final String GOOGLE_TRANSLATE_URL =
            "https://translation.googleapis.com/language/translate/v2";

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

                String translatedText = googleTranslate(text.toString(), sourceLang, targetLang);

                translatedContent.append(String.format("%s\n%s\n%s\n\n", number, time, translatedText.trim()));
            } else {
                translatedContent.append(entry).append("\n\n");
            }
        }

        return translatedContent.toString().trim();
    }

    private String googleTranslate(String text, String sourceLang, String targetLang) throws IOException {
        String url = GOOGLE_TRANSLATE_URL +
                "?key=" + googleApiKey +
                "&q=" + URLEncoder.encode(text, StandardCharsets.UTF_8) +
                "&source=" + sourceLang +
                "&target=" + targetLang +
                "&format=text";

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Google Translate failed: " + response.code());
            }

            String responseBody = response.body().string();
            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            return json.getAsJsonObject("data")
                    .getAsJsonArray("translations")
                    .get(0).getAsJsonObject()
                    .get("translatedText").getAsString();
        }
    }
}
