package com.bkap.aispark.service;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Service
public class AudioTTSService {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.api.tts.url:https://api.openai.com/v1/audio/speech}")
    private String openAiTtsUrl;

    private static final MediaType JSON = MediaType.parse("application/json");
    private final OkHttpClient client = new OkHttpClient();

    /**
     * Tạo file âm thanh từ danh sách câu văn bản
     * @param texts danh sách phụ đề hoặc câu thoại
     * @return danh sách đường dẫn file mp3 được tạo
     */
    public List<String> generateAudioFiles(List<String> texts) throws IOException {
        List<String> audioFiles = new ArrayList<>();

        for (int i = 0; i < texts.size(); i++) {
            String text = texts.get(i);
            String filename = "audio_" + (i + 1) + ".mp3";

            String fileUrl = createTtsFile(text, filename);
            audioFiles.add(fileUrl);
        }

        return audioFiles;
    }

    /**
     * Gọi OpenAI TTS API để tạo file MP3
     */
    private String createTtsFile(String text, String filename) throws IOException {
        JSONObject json = new JSONObject();
        json.put("model", "gpt-4o-mini-tts");
        json.put("voice", "alloy"); // Giọng nữ tự nhiên, có thể đổi sang "verse"
        json.put("input", text);

        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(openAiTtsUrl)
                .addHeader("Authorization", "Bearer " + openAiApiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        File outputDir = new File("tts");
        if (!outputDir.exists()) outputDir.mkdirs();

        File outputFile = new File(outputDir, filename);

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("TTS API error: " + response.code() + " - " + response.body().string());
            }

            byte[] audioData = response.body().bytes();
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(audioData);
            }

            // 👉 Ở đây bạn có thể upload lên R2 hoặc S3, tạm trả về local path
            return outputFile.getAbsolutePath();
        }
    }
}
