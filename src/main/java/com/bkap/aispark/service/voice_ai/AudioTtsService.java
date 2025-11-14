package com.bkap.aispark.service.voice_ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class AudioTtsService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate rest = new RestTemplate();

    public String toSpeech(String text, String voice) {
        try {
            String url = "https://api.openai.com/v1/audio/speech";

            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(apiKey);
            h.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini-tts",
                    "voice", voice,
                    "input", text
            );

            ResponseEntity<byte[]> res = rest.exchange(
                    url, HttpMethod.POST,
                    new HttpEntity<>(body, h),
                    byte[].class
            );

            return Base64.getEncoder().encodeToString(res.getBody());
        } catch (Exception e) {
            log.error("TTS error", e);
            return "";
        }
    }
}