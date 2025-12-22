package com.bkap.aispark.service;

import com.bkap.aispark.entity.Storybook;
import com.bkap.aispark.entity.StorybookAiConfig;
import com.bkap.aispark.entity.StoryGenerationResult;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
public class GeminiClientService {

	@Value("${gemini.api.key}")
	private String apiKey;

	private final Gson gson = new Gson();

	//Text

	public StoryGenerationResult generateStructuredStory(Storybook storybook, StorybookAiConfig config) {
		try (Client client = new Client()) {

			String prompt = """
					Bạn là chuyên gia viết truyện thiếu nhi tiếng Việt.
					Dựa trên ý tưởng: "%s"

					Hãy trả về DUY NHẤT JSON sau (không thêm chữ khác):
					{
					  "title": "Tiêu đề",
					  "description": "Mô tả",
					  "pages": [
					    {
					      "text_content": "Nội dung...",
					      "image_prompt": "English image prompt..."
					    }
					  ]
					}
					""".formatted(storybook.getOriginalPrompt());

			GenerateContentResponse response = client.models.generateContent(config.getTextModel(), 
					prompt, null);

			String json = cleanJson(response.text());
			return gson.fromJson(json, StoryGenerationResult.class);
		}
	}

	//Img

	public byte[] generateImageBytes(String imagePrompt, StorybookAiConfig config) {
		try (Client client = new Client()) {

			GenerateContentConfig genConfig =
			        GenerateContentConfig.builder()
			                .responseModalities(List.of("TEXT", "IMAGE"))
			                .build();


			GenerateContentResponse response = client.models.generateContent(config.getImageModel(), 
					imagePrompt, genConfig);

			for (Part part : response.parts()) {
				if (part.inlineData().isPresent() && part.inlineData().get().data().isPresent()) {

					return part.inlineData().get().data().get();
				}
			}

			throw new RuntimeException("No image generated");
		}
	}

	//TTS

	public byte[] generateTts(String text, StorybookAiConfig config) {
		try (Client client = new Client()) {

			GenerateContentConfig genConfig =
			        GenerateContentConfig.builder()
			                .responseModalities(List.of("AUDIO"))
			                .build();

			GenerateContentResponse response = client.models.generateContent(config.getTtsModel(), 
					text, genConfig);

			for (Part part : response.parts()) {
				if (part.inlineData().isPresent() && part.inlineData().get().data().isPresent()) {

					return part.inlineData().get().data().get(); // PCM bytes
				}
			}

			throw new RuntimeException("No audio generated");
		}
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
