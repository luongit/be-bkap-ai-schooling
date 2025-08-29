package com.bkap.aispark.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    OpenAiService openAiService() {
        return new OpenAiService(apiKey);
    }
    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper()
            .configure(JsonGenerator.Feature.ESCAPE_NON_ASCII, false)
            .configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), false);
    }

}
