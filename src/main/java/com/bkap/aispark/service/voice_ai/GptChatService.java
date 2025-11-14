package com.bkap.aispark.service.voice_ai;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GptChatService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate rest = new RestTemplate();

    public String ask(String system, String user) {

        try {
            String url = "https://api.openai.com/v1/chat/completions";

            HttpHeaders h = new HttpHeaders();
            h.setBearerAuth(apiKey);
            h.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "system", "content", system),
                            Map.of("role", "user", "content", user)
                    )
            );

            Map<?, ?> response = rest.postForObject(url, new HttpEntity<>(body, h), Map.class);

            return (String) ((Map<?, ?>)
                    ((Map<?, ?>) ((List<?>) response.get("choices")).get(0))
                            .get("message"))
                    .get("content");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("GPT error: " + e.getMessage());
        }
    }
}
