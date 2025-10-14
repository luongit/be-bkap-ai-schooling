package com.bkap.aispark.service;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OpenRouterImageService {

    private final List<String> apiKeys;
    private final AtomicInteger keyIndex = new AtomicInteger(0);

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model.id}")
    private String modelId;

    public OpenRouterImageService(@Value("${openrouter.api.keys}") String apiKeysStr) {
        this.apiKeys = Arrays.asList(apiKeysStr.split(","));
        System.out.println("🔑 Loaded " + apiKeys.size() + " OpenRouter keys for rotation.");
    }

    private String getNextKey() {
        int index = keyIndex.getAndUpdate(i -> (i + 1) % apiKeys.size());
        return apiKeys.get(index).trim();
    }

    private String getCurrentKey() {
        return apiKeys.get(keyIndex.get() % apiKeys.size()).trim();
    }

    public String generateImage(String prompt, String style, String size) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        MediaType mediaType = MediaType.parse("application/json");
        String json = "{"
                + "\"model\": \"" + modelId + "\","
                + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}],"
                + "\"temperature\": 0.7"
                + "}";

        for (int i = 0; i < apiKeys.size(); i++) {
            String currentKey = getCurrentKey();

            try {
                RequestBody body = RequestBody.create(mediaType, json);
                Request request = new Request.Builder()
                        .url(apiUrl)
                        .addHeader("Authorization", "Bearer " + currentKey)
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    String errBody = response.body() != null ? response.body().string() : "Unknown error";
                    int code = response.code();

                    if (code == 401 || code == 403 || code == 429) {
                        System.err.println("⚠️ Key lỗi hoặc hết quota (" + currentKey + "), chuyển key khác...");
                        getNextKey();
                        continue;
                    }

                    throw new RuntimeException("❌ API error: " + errBody);
                }

                String responseBody = response.body().string();
                JSONObject responseJson = new JSONObject(responseBody);
                JSONArray choices = responseJson.getJSONArray("choices");
                JSONObject firstChoice = choices.getJSONObject(0);
                JSONObject message = firstChoice.getJSONObject("message");
                JSONArray images = message.getJSONArray("images");
                JSONObject imageObj = images.getJSONObject(0);
                JSONObject imageUrlObj = imageObj.getJSONObject("image_url");
                String imageUrl = imageUrlObj.getString("url");

                System.out.println("✅ Sinh ảnh thành công với key: " + currentKey);
                return imageUrl;

            } catch (IOException e) {
                System.err.println("⚠️ Lỗi mạng hoặc timeout với key: " + currentKey);
                getNextKey();
            } catch (Exception e) {
                throw new RuntimeException("❌ Image generation failed: " + e.getMessage());
            }
        }

        throw new RuntimeException("❌ Tất cả key đều lỗi hoặc hết quota!");
    }
}
