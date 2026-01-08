package com.bkap.aispark.service;

import com.bkap.aispark.entity.Storybook.StoryGenerationResult;
import com.bkap.aispark.entity.Storybook.Storybook;
import com.bkap.aispark.entity.Storybook.StorybookAiConfig;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Component
public class GeminiClientService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final Gson gson = new Gson();

   

    private Client createClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

  

    public StoryGenerationResult generateStructuredStory(
            Storybook storybook,
            StorybookAiConfig config
    ) {

        try (Client client = createClient()) {

            String prompt = """
                    Bạn là chuyên gia viết truyện thiếu nhi tiếng Việt.

                    Dựa trên ý tưởng: "%s"

                    YÊU CẦU:
                    - Văn phong nhẹ nhàng, phù hợp trẻ em
                    - Mỗi trang 1 đoạn ngắn
                    - Image prompt viết bằng TIẾNG ANH, chi tiết, phù hợp tranh thiếu nhi

                    CHỈ TRẢ VỀ JSON (không thêm chữ):
                    {
                      "title": "Tiêu đề",
                      "description": "Mô tả",
                      "pages": [
                        {
                          "text_content": "Nội dung trang",
                          "image_prompt": "English image prompt"
                        }
                      ]
                    }

                    QUAN TRỌNG:
                    - Không dùng dấu " chưa escape
                    - Không dùng ký tự đặc biệt
                    - Không xuống dòng trong text_content
                    - JSON phải hợp lệ tuyệt đối
                    """.formatted(storybook.getOriginalPrompt());

            GenerateContentResponse response =
                    client.models.generateContent(
                            config.getTextModel(), 
                            prompt,
                            null
                    );

            String raw = cleanJson(response.text());

            try {
                return gson.fromJson(raw, StoryGenerationResult.class);
            }
            catch (Exception ex) {

                System.err.println("❌ RAW JSON FROM GEMINI:");
                System.err.println(raw);

                String fixed = sanitizeJson(raw);

                System.err.println("🛠 FIXED JSON:");
                System.err.println(fixed);

                return gson.fromJson(fixed, StoryGenerationResult.class);
            }
        }
    }

    

    public byte[] generateImageBytes(
            String imagePrompt,
            StorybookAiConfig config
    ) {

        try (Client client = createClient()) {

            GenerateContentConfig genConfig =
                    GenerateContentConfig.builder()
                            .responseModalities(List.of("TEXT", "IMAGE"))
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            config.getImageModel(),
                            imagePrompt,
                            genConfig
                    );

            for (Part part : response.parts()) {
                if (part.inlineData().isPresent()
                        && part.inlineData().get().data().isPresent()) {

                    return part.inlineData().get().data().get();
                }
            }

            throw new RuntimeException("Gemini image generation returned no image");
        }
    }

   

    public byte[] generateTtsWav(
            String text,
            StorybookAiConfig config
    ) {

        try (Client client = createClient()) {

            SpeechConfig speechConfig =
                    SpeechConfig.builder()
                            .voiceConfig(
                                    VoiceConfig.builder()
                                            .prebuiltVoiceConfig(
                                                    PrebuiltVoiceConfig.builder()
                                                            .voiceName("Kore")
                                                            .build()
                                            )
                                            .build()
                            )
                            .build();

            GenerateContentConfig genConfig =
                    GenerateContentConfig.builder()
                            .responseModalities(List.of("AUDIO"))
                            .speechConfig(speechConfig)
                            .build();

            GenerateContentResponse response =
                    client.models.generateContent(
                            config.getTtsModel(),
                            text,
                            genConfig
                    );

            for (Part part : response.parts()) {
                if (part.inlineData().isPresent()
                        && part.inlineData().get().data().isPresent()) {

                    byte[] pcm = part.inlineData().get().data().get();

                    if (isAllZero(pcm)) {
                        throw new RuntimeException("Gemini TTS returned silence");
                    }

                    return pcmToWav(pcm, 24000, 1);
                }
            }

            throw new RuntimeException("No audio generated from Gemini");

        } catch (IOException e) {
            throw new RuntimeException("Failed to convert PCM to WAV", e);
        }
    }

    /* ================= UTIL ================= */

    private boolean isAllZero(byte[] pcm) {
        for (byte b : pcm) {
            if (b != 0) return false;
        }
        return true;
    }

    private byte[] pcmToWav(byte[] pcm, int sampleRate, int channels)
            throws IOException {

        int byteRate = sampleRate * channels * 2;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        out.write("RIFF".getBytes());
        out.write(intLE(36 + pcm.length));
        out.write("WAVEfmt ".getBytes());
        out.write(intLE(16));
        out.write(shortLE((short) 1));
        out.write(shortLE((short) channels));
        out.write(intLE(sampleRate));
        out.write(intLE(byteRate));
        out.write(shortLE((short) (channels * 2)));
        out.write(shortLE((short) 16));
        out.write("data".getBytes());
        out.write(intLE(pcm.length));
        out.write(pcm);

        return out.toByteArray();
    }

    private byte[] intLE(int v) {
        return new byte[]{
                (byte) v,
                (byte) (v >> 8),
                (byte) (v >> 16),
                (byte) (v >> 24)
        };
    }

    private byte[] shortLE(short v) {
        return new byte[]{
                (byte) v,
                (byte) (v >> 8)
        };
    }

    /*JSon*/

    private String cleanJson(String raw) {

        raw = raw.replaceAll("```json|```", "").trim();

        int s = raw.indexOf('{');
        int e = raw.lastIndexOf('}');

        if (s >= 0 && e > s) {
            return raw.substring(s, e + 1);
        }

        throw new RuntimeException("Invalid JSON from Gemini");
    }

    /* JSON SANITIZE FIX */

    private String sanitizeJson(String json) {

        // remove control chars
        json = json.replaceAll("[\\x00-\\x1F]", " ");

        // escape "
        json = json.replaceAll("(?<!\\\\)\"", "\\\\\"");

        // normalize newlines
        json = json.replace("\n", "\\n");

        // ensure braces
        if (!json.startsWith("{")) json = "{" + json;
        if (!json.endsWith("}")) json = json + "}";

        return json;
    }
}
