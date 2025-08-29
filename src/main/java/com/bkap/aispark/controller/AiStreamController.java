package com.bkap.aispark.controller;

import com.bkap.aispark.helper.LatexNormalizer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jakarta.servlet.http.HttpServletResponse; // <— thêm import này
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
public class AiStreamController {

    @Autowired
    private OpenAiService openAiService;

    private static final MediaType NDJSON = MediaType.valueOf("application/x-ndjson");
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Body nhận:
     * {
     *   "messages": [{ "role":"user"|"assistant"|"system", "content":"..." }, ...],
     *   "audience": "student|teacher|kid|general" (optional)
     * }
     */
    @SuppressWarnings("unchecked")
    @PostMapping(path = "/stream", produces = "application/x-ndjson")
    public ResponseBodyEmitter stream(@RequestBody Map<String, Object> body, HttpServletResponse resp) {
        // —— Headers chống buffer/nén cho proxy (IIS/ARR, CDN) ——
        resp.setHeader("Content-Type", "application/x-ndjson; charset=utf-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");
        resp.setHeader("Connection", "keep-alive");
        // Với Nginx hữu ích, IIS sẽ bỏ qua nhưng không hại:
        resp.setHeader("X-Accel-Buffering", "no");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L); // no timeout

        CompletableFuture.runAsync(() -> {
            // 1) Lấy input & xây prompt
            List<Map<String, String>> messagesData = (List<Map<String, String>>) body.get("messages");
            String audience = Optional.ofNullable((String) body.get("audience")).orElse("general");

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", buildSystemPrompt(audience)));

            if (messagesData != null) {
                for (Map<String, String> m : messagesData) {
                    String role = m.get("role");
                    String content = m.get("content");
                    if (role != null && content != null) {
                        messages.add(new ChatMessage(role, content));
                    }
                }
            }

            try {
                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model("gpt-4o")
                        .messages(messages)
                        .temperature(0.2)     // chính xác học thuật
                        .stream(true)
                        .build();

                // 2) Chuẩn hoá khi stream
                final StringBuilder safeBuffer = new StringBuilder();
                final LatexNormalizer normalizer = new LatexNormalizer();

                openAiService.streamChatCompletion(request)
                        .doOnError(err -> {
                            try {
                                Map<String, String> error = new HashMap<>();
                                error.put("type", "error");
                                error.put("message", err.getMessage());
                                emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
                            } catch (Exception ignored) {}
                        })
                        .blockingForEach(chunk -> {
                            try {
                                var choices = chunk.getChoices();
                                if (choices == null || choices.isEmpty()) return;

                                String content = extractChoiceContent(choices.get(0));
                                if (content == null || content.isEmpty()) return;

                                safeBuffer.append(content);
                                String emitChunk = normalizer.tryNormalizeAndExtractStablePrefix(safeBuffer);
                                if (!emitChunk.isEmpty()) {
                                    Map<String, String> json = new HashMap<>();
                                    json.put("type", "chunk");
                                    json.put("role", "assistant");
                                    json.put("content", emitChunk);
                                    emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);
                                }
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        });

                // 3) Flush phần còn lại
                String tail = normalizer.flushAll(safeBuffer);
                if (!tail.isEmpty()) {
                    Map<String, String> json = new HashMap<>();
                    json.put("type", "chunk");
                    json.put("role", "assistant");
                    json.put("content", tail);
                    emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);
                }

                // 4) Tín hiệu kết thúc
                Map<String, String> done = new HashMap<>();
                done.put("type", "done");
                emitter.send(objectMapper.writeValueAsString(done) + "\n", NDJSON);

                emitter.complete();
            } catch (Exception e) {
                try {
                    Map<String, String> error = new HashMap<>();
                    error.put("type", "error");
                    error.put("message", e.getMessage());
                    emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
                } catch (Exception ignored) {}
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // Prompt hệ thống
    private String buildSystemPrompt(String audience) {
        String tone;
        switch (audience) {
            case "kid":     tone = "Giải thích thật dễ hiểu, ví dụ gần gũi, câu ngắn, dùng emoji tiết chế 👦👧."; break;
            case "student": tone = "Ngắn gọn, đi thẳng ý, có ví dụ và bài tập nhỏ."; break;
            case "teacher": tone = "Chuẩn xác, có định nghĩa, tính chất, ví dụ mẫu và gợi ý chấm điểm."; break;
            default:        tone = "Thân thiện, rõ ràng, có ví dụ minh hoạ."; break;
        }

        return String.join("\n",
                "Bạn là trợ lý học tập tiếng Việt phục vụ học sinh, sinh viên, trẻ nhỏ và giáo viên.",
                "YÊU CẦU ĐỊNH DẠNG và CHUẨN HOÁ XUẤT RA:",
                "- Dùng Markdown. Không dùng HTML thô.",
                "- Công thức toán học: inline dùng $...$, block dùng $$...$$. Không dùng \\( ... \\) hoặc \\[ ... \\].",
                "- Với block math, đặt công thức trên dòng riêng giữa hai dấu $$.",
                "- Không bọc công thức trong code fence ```...```.",
                "- Khi cần liệt kê, dùng danh sách Markdown hoặc bảng Markdown.",
                "- Nếu viết ký hiệu như pi, dùng \\pi; phân số dùng \\frac{...}{...}; tích dùng \\times; chia dùng \\div.",
                "- Khi nêu công thức kèm điều kiện (ví dụ b ≠ 0) dùng inline math: $(b \\ne 0)$.",
                "",
                "PHONG CÁCH TRÌNH BÀY:",
                tone,
                "- Khi trả lời theo chương trình Bộ GD&ĐT Việt Nam, dùng thuật ngữ chuẩn, ví dụ: 'Trung bình cộng', 'BCNN', 'ƯCLN', ...",
                "- Ưu tiên tính chính xác, ngắn gọn. Khi cần, thêm ví dụ minh hoạ ngắn.",
                "- Luôn gợi ý 3 - 5 cấu hỏi hoặc đề xuất định hướng gợi ý phù hợp với nội dung mà người dùng đang hỏi",
                ""
        );
    }

    // Tương thích nhiều phiên bản theokanning
    private String extractChoiceContent(Object choice) {
        if (choice == null) return null;
        try {
            var getDelta = choice.getClass().getMethod("getDelta");
            Object delta = getDelta.invoke(choice);
            if (delta != null) {
                var getContent = delta.getClass().getMethod("getContent");
                Object c = getContent.invoke(delta);
                if (c != null) return c.toString();
            }
        } catch (NoSuchMethodException ignore) {
        } catch (Exception ignore) {}

        try {
            var getMessage = choice.getClass().getMethod("getMessage");
            Object msg = getMessage.invoke(choice);
            if (msg != null) {
                var getContent = msg.getClass().getMethod("getContent");
                Object c = getContent.invoke(msg);
                if (c != null) return c.toString();
            }
        } catch (Exception ignore) {}

        try {
            var getText = choice.getClass().getMethod("getText");
            Object t = getText.invoke(choice);
            if (t != null) return t.toString();
        } catch (Exception ignore) {}

        return null;
    }
}
