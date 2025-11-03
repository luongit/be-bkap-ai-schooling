package com.bkap.aispark.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

@Service
public class AiService {

    @Autowired
    private OpenAiService openAiService;

    public Map<String, Object> gradeExercise(String questionJson, String answerJson) {
        String prompt = """
            Bạn là giáo viên. Hãy chấm điểm bài làm của học sinh theo thang 10.
            Câu hỏi: %s
            Trả lời học sinh: %s
            Trả về JSON có dạng: {"score": số, "feedback": "nhận xét ngắn gọn"}.
            """.formatted(questionJson, answerJson);

        // Tạo request chat completion
        ChatCompletionRequest request = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo") // hoặc "gpt-4o-mini" nếu bạn dùng model khác
                .messages(List.of(
                        new ChatMessage("system", "Bạn là giáo viên chấm bài."),
                        new ChatMessage("user", prompt)
                ))
                .maxTokens(200)
                .temperature(0.3)
                .build();

        // Gọi OpenAI API
        ChatCompletionResult result = openAiService.createChatCompletion(request);

        // Lấy nội dung trả về
        String content = result.getChoices().get(0).getMessage().getContent();

        try {
            // Parse JSON trả về
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(content, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            // Nếu AI trả về không đúng JSON, fallback
            return Map.of("score", 0, "feedback", "Phản hồi không hợp lệ: " + content);
        }
    }
}


