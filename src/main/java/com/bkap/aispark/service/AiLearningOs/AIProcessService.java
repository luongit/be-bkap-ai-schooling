package com.bkap.aispark.service.AiLearningOs;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
        // Tạo body request
        JSONObject body = new JSONObject()
                .put("model", modelName)
                .put("messages", new JSONArray()
                        .put(new JSONObject()
                                .put("role", "user")
                                .put("content", prompt)));

        // Thiết lập headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        // Gửi request tới OpenAI API
        HttpEntity<String> request = new HttpEntity<>(body.toString(), headers);
        ResponseEntity<String> response = rest.exchange(
                OPENAI_URL,
                HttpMethod.POST,
                request,
                String.class);

        // Chuyển đổi kết quả trả về từ OpenAI thành JSONObject
        JSONObject json = new JSONObject(response.getBody());

        // Lấy nội dung trả về từ OpenAI
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
    if (aiOutput == null || aiOutput.trim().isEmpty()) {
        return "{}";  // Trả về "{}" nếu aiOutput là null hoặc rỗng
    }

    // Loại bỏ codeblock json và các ký tự dư thừa (nếu có)
    String cleaned = aiOutput
            .replace("```json", "")
            .replace("```", "")
            .trim();

    // Kiểm tra nếu output không phải là JSON hợp lệ
    if (!cleaned.startsWith("{") && !cleaned.startsWith("[")) {
        System.err.println("❌ AI output is not valid JSON. Raw = " + aiOutput);
        return "{}";  // Trả về JSON rỗng nếu không hợp lệ
    }

    try {
        // Cố gắng phân tích đầu ra của AI thành một JSONObject
        new JSONObject(cleaned);
    } catch (Exception e) {
        System.err.println("❌ AI output is invalid JSON format: " + aiOutput);
        return "{}";  // Trả về JSON rỗng nếu không thể phân tích
    }

    // Nếu output hợp lệ, trả về JSON sạch sẽ
    return cleaned;
}

}
