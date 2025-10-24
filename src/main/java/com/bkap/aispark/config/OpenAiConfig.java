package com.bkap.aispark.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    OpenAiService openAiService() {
        // ✅ Tăng timeout từ 10s mặc định lên 120s
        return new OpenAiService(apiKey, Duration.ofSeconds(120));
    }

    @Bean
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // đăng ký module để hỗ trợ LocalDate, LocalDateTime...
        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

        // cấu hình không escape Unicode
        mapper.configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false);
        mapper.configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);

        // tránh serialize LocalDateTime thành timestamp (epoch millis)
        mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        return mapper;
    }

}
