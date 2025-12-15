package com.bkap.aispark.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentSummarizerService {

    private final OpenAiService openAiService;

    public DocumentSummarizerService(OpenAiService openAiService) {
        this.openAiService = openAiService;
    }

    public String summarizeOnce(String model, String rawText) {
        // Prompt tóm tắt kiểu “kiến thức nền cho chatbot”
        String sys = """
                Bạn là trợ lý tóm tắt tài liệu để làm "knowledge base" cho chatbot.
                Hãy tạo bản tóm tắt có cấu trúc rõ ràng:
                - Định nghĩa/khái niệm chính
                - Quy tắc/luật/khuyến nghị quan trọng
                - Quy trình/các bước (nếu có)
                - Các trường hợp/ngoại lệ
                - Ví dụ minh hoạ (nếu có)
                Giữ thông tin chuẩn, không bịa.
                """;

        String user = "Tài liệu:\n\n" + rawText;

        ChatCompletionRequest req = ChatCompletionRequest.builder()
                .model(model)
                .messages(List.of(
                        new ChatMessage("system", sys),
                        new ChatMessage("user", user)
                ))
                .temperature(0.2)
                .build();

        var res = openAiService.createChatCompletion(req);
        return res.getChoices().get(0).getMessage().getContent();
    }
}
