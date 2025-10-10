package com.bkap.aispark.service;

import okhttp3.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class OpenRouterImageService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model.id}")
    private String modelId;

    public String generateImage(String prompt, String style, String size) {
        try {
        	OkHttpClient client = new OkHttpClient.Builder()
        		    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS) // thời gian chờ kết nối
        		    .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)    // thời gian chờ đọc dữ liệu
        		    .writeTimeout(60, java.util.concurrent.TimeUnit.SECONDS)   // thời gian chờ ghi dữ liệu
        		    .build();


            MediaType mediaType = MediaType.parse("application/json");
            String json = "{"
                    + "\"model\": \"" + modelId + "\","
                    + "\"messages\": [{\"role\": \"user\", \"content\": \"" + prompt + "\"}],"
                    + "\"temperature\": 0.7"
                    + "}";

            RequestBody body = RequestBody.create(mediaType, json);
            Request request = new Request.Builder()
                    .url(apiUrl)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                throw new RuntimeException("❌ API error: " + errorBody);
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

            return imageUrl;


        } catch (Exception e) {
            throw new RuntimeException("Image generation failed: " + e.getMessage());
        }
    }
}
