package com.bkap.aispark.service;

import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookAiConfig;
import com.bkap.aispark.entity.StoryGenerationResult;
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

    /* ================= CLIENT ================= */

    private Client createClient() {
        return Client.builder()
                .apiKey(apiKey)
                .build();
    }

    /* ================= TEXT (STORY) ================= */

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
                    """.formatted(storybook.getOriginalPrompt());

            GenerateContentResponse response =
                    client.models.generateContent(
                            config.getTextModel(), // gemini-1.5-flash
                            prompt,
                            null
                    );

            String json = cleanJson(response.text());
            return gson.fromJson(json, StoryGenerationResult.class);
        }
    }

    /* ================= IMAGE (NANO BANANA) ================= */

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
                            config.getImageModel(), // gemini-2.5-flash-image
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

    /* ================= TTS (AUDIO → WAV) ================= */

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
                            config.getTtsModel(), // gemini-1.5-flash
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

    private String cleanJson(String raw) {
        raw = raw.replaceAll("```json|```", "").trim();
        int s = raw.indexOf('{');
        int e = raw.lastIndexOf('}');
        if (s >= 0 && e > s) {
            return raw.substring(s, e + 1);
        }
        throw new RuntimeException("Invalid JSON from Gemini");
    }
}
