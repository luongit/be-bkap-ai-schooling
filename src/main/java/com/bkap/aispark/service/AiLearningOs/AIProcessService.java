package com.bkap.aispark.service.AiLearningOs;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AIProcessService {

        @Value("${openai.api.key}")
        private String apiKey;

        @Value("${openai.model:gpt-4o-mini}")
        private String modelName;

        private final RestTemplate rest = new RestTemplate();

        private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

        /**
         * ------------------------
         * 1. Summary
         * ------------------------
         */
        public String generateSummary(String rawText) {
                String prompt = """
                                Hãy tóm tắt nội dung sau thành 150–250 từ, dễ hiểu, rõ ràng:

                                """ + rawText;

                return askOpenAI(prompt);
        }

        /**
         * ------------------------
         * 2. Knowledge Node Extraction
         * ------------------------
         */
        public String extractKnowledgeNodes(String rawText) {
                String prompt = """
                                Hãy phân tích nội dung sau và trích xuất Knowledge Nodes.
                                Output JSON:
                                {
                                  "topics": [],
                                  "key_concepts": [],
                                  "relations": []
                                }

                                """ + rawText;

                return askOpenAI(prompt);
        }

        /**
         * ------------------------
         * 3. Quiz generator
         * ------------------------
         */
        public String generateQuiz(String rawText) {
                String prompt = """
                                Tạo 10 câu hỏi trắc nghiệm từ nội dung sau.
                                Output JSON:
                                [
                                  {
                                    "question": "",
                                    "options": ["A", "B", "C", "D"],
                                    "answer": "",
                                    "explanation": ""
                                  }
                                ]

                                """ + rawText;

                return askOpenAI(prompt);
        }

        /**
         * ------------------------
         * CORE: CALL OPENAI
         * ------------------------
         */
        private String askOpenAI(String prompt) {
                try {
                        JSONObject body = new JSONObject()
                                        .put("model", modelName)
                                        .put("messages", new JSONArray()
                                                        .put(new JSONObject()
                                                                        .put("role", "user")
                                                                        .put("content", prompt)));

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.set("Authorization", "Bearer " + apiKey);

                        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);

                        ResponseEntity<String> response = rest.exchange(
                                        OPENAI_URL,
                                        HttpMethod.POST,
                                        request,
                                        String.class);

                        JSONObject json = new JSONObject(response.getBody());

                        return json
                                        .getJSONArray("choices")
                                        .getJSONObject(0)
                                        .getJSONObject("message")
                                        .getString("content");

                } catch (Exception e) {
                        e.printStackTrace();
                        return "AI_PROCESSING_ERROR";
                }
        }

        public String cleanJson(String aiOutput) {
                if (aiOutput == null)
                        return "{}";

                // Xoá ký tự codeblock: ```json ... ```
                String cleaned = aiOutput
                                .replace("```json", "")
                                .replace("```", "")
                                .trim();

                // Xóa ký tự ` đơn lẻ
                if (cleaned.startsWith("`") && cleaned.endsWith("`")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                }

                // Nếu AI trả text không phải JSON → fallback về {}
                if (!cleaned.startsWith("{") && !cleaned.startsWith("[")) {
                        System.err.println("❌ AI output is not valid JSON. Raw = " + aiOutput);
                        return "{}";
                }

                return cleaned;
        }
}
