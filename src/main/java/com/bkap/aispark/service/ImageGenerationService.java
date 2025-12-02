package com.bkap.aispark.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.*;

@Service
public class ImageGenerationService {

	private static final String API_URL = "https://cloud.leonardo.ai/api/rest/v1/generations";

	private final R2StorageService r2;
	private final UserImageHistoryService history;
	private final ImageLibraryService libraryService;
	private final RestTemplate rest = new RestTemplate();

	@Value("${leonardo.api.key}")
	private String apiKey;

	public ImageGenerationService(R2StorageService r2, UserImageHistoryService history,
			ImageLibraryService libraryService) {
		this.r2 = r2;
		this.history = history;
		this.libraryService = libraryService;
	}

	@Autowired
	private OpenAiService openAiService;

	//Generate image using Leonardo API – 2-step flow
	 
	public String generate(Long userId, String prompt, String styleUUID, String size) {
		try {

			// 1) Check slot
			if (!libraryService.canStore(userId)) {
				throw new RuntimeException("LIMIT_REACHED");
			}

			// 2) Parse size
			int width = 1024;
			int height = 1024;
			try {
				String[] parts = size.toLowerCase().split("x");
				width = Integer.parseInt(parts[0]);
				height = Integer.parseInt(parts[1]);
			} catch (Exception ignored) {
			}

			// 3) Build POST body
			Map<String, Object> body = new HashMap<>();
			body.put("modelId", "b24e16ff-06e3-43eb-8d33-4416c2d75876");
			String englishPrompt = translatePrompt(prompt);
			body.put("prompt", englishPrompt);

			body.put("height", height);
			body.put("width", width);
			body.put("num_images", 1);
			body.put("alchemy", false);

			// 4) Headers
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("accept", "application/json");
			headers.set("authorization", "Bearer " + apiKey);

			HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

			// 5) Create job (POST)
			ResponseEntity<Map> resp = rest.exchange(API_URL, HttpMethod.POST, request, Map.class);

			Map<String, Object> result = resp.getBody();
			if (result == null || result.get("sdGenerationJob") == null)
				throw new RuntimeException("EMPTY_RESPONSE");

			String generationId = ((Map) result.get("sdGenerationJob")).get("generationId").toString();

			// 6) Poll job until COMPLETE
			String finalImageUrl = pollResult(generationId);

			// 7) Download & upload R2
			byte[] imageBytes = rest.getForObject(finalImageUrl, byte[].class);

			FakeMultipartFile file = new FakeMultipartFile(UUID.randomUUID() + ".jpg", "image/jpeg", imageBytes);

			String r2Url = r2.uploadFile(file);

			// 8) Increase slot
			libraryService.incrementUsed(userId);

			// 9) Save history
			history.save(userId, prompt, styleUUID, size, r2Url, "SUCCESS", null);

			return r2Url;

		} catch (Exception e) {

			String msg = e.getMessage();

			// Safety
			if (msg.contains("safety")) {
				String safe = "Ảnh chứa nội dung bị hạn chế. Vui lòng mô tả lại.";
				history.save(userId, prompt, styleUUID, size, null, "ERROR", safe);
				throw new RuntimeException(safe);
			}

			// Timeout
			if (msg.contains("timeout")) {
				String timeout = "Hệ thống đang quá tải. Vui lòng thử lại.";
				history.save(userId, prompt, styleUUID, size, null, "ERROR", timeout);
				throw new RuntimeException(timeout);
			}

			// Other
			String generic = "Lỗi tạo ảnh : " + msg;
			history.save(userId, prompt, styleUUID, size, null, "ERROR", generic);
			throw new RuntimeException(generic);
		}
	}

	// Poll GET until generation is complete 
	private String pollResult(String generationId) throws InterruptedException {

		String url = API_URL + "/" + generationId;

		HttpHeaders headers = new HttpHeaders();
		headers.set("authorization", "Bearer " + apiKey);
		headers.set("accept", "application/json");

		HttpEntity<Void> entity = new HttpEntity<>(headers);

		for (int i = 0; i < 20; i++) { // ~20 seconds
			ResponseEntity<Map> resp = rest.exchange(url, HttpMethod.GET, entity, Map.class);

			Map body = resp.getBody();
			if (body == null)
				continue;

			Map gen = (Map) body.get("generations_by_pk");
			if (gen == null)
				continue;

			String status = (String) gen.get("status");
			if ("COMPLETE".equals(status)) {

				List<Map<String, Object>> imgs = (List<Map<String, Object>>) gen.get("generated_images");

				if (imgs != null && !imgs.isEmpty()) {
					return imgs.get(0).get("url").toString();
				}
			}

			Thread.sleep(1000);
		}

		throw new RuntimeException("TIMEOUT_NO_GENERATION");
	}

	public String translatePrompt(String prompt) {
	    try {
	        ChatCompletionRequest req = ChatCompletionRequest.builder()
	            .model("gpt-4o")
	            .messages(List.of(
	                new ChatMessage("system",
	                    """
	                    You are a translation assistant.
	                    Translate the given Vietnamese image description into natural English.
	                    
	                    RULES:
	                    - DO NOT add any new visual details.
	                    - DO NOT expand the scene.
	                    - DO NOT add lighting, environment, effects, or composition.
	                    - Translate EXACTLY the meaning of the user's text.
	                    - Keep the output short, simple, clear.
	                    - Return ONLY the English translation.
	                    """
	                ),
	                new ChatMessage("user", prompt)
	            ))
	            .maxTokens(60)
	            .temperature(0.0)
	            .build();

	        var res = openAiService.createChatCompletion(req);
	        return res.getChoices().get(0).getMessage().getContent().trim();

	    } catch (Exception e) {
	        return prompt; // fallback: dùng đúng prompt gốc nếu OpenAI lỗi
	    }
	}




}
