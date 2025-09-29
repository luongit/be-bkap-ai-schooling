package com.bkap.aispark.controller;

import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AiChatService;
import com.bkap.aispark.service.ConversationLogService;
import com.bkap.aispark.service.CreditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/writing")
public class AiWritingController {

    @Autowired
    private OpenAiService openAiService;

    private static final MediaType NDJSON = MediaType.valueOf("application/x-ndjson");
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ConversationLogService conversationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private CreditService creditService;

    /**
     * Body nhận: {
     *   "prompt": "Phân tích tác phẩm",
     *   "tone": "long|medium|outline" (optional, default: medium),
     *   "session_id": "UUID string"
     * }
     */
    @PostMapping(path = "/stream", produces = "application/x-ndjson")
    public ResponseBodyEmitter streamWriting(@RequestBody Map<String, String> body, HttpServletRequest httpRequest,
                                            HttpServletResponse resp) {
        resp.setHeader("Content-Type", "application/x-ndjson; charset=utf-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Expires", "0");
        resp.setHeader("Connection", "keep-alive");
        resp.setHeader("X-Accel-Buffering", "no");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L);

        CompletableFuture.runAsync(() -> {
            final StringBuilder fullResponse = new StringBuilder();
            try {
                // 1) Lấy userId từ JWT token
                String authHeader = httpRequest.getHeader("Authorization");
                Long userId = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    String token = authHeader.substring(7);
                    userId = jwtUtil.getUserId(token);
                } else {
                    throw new RuntimeException("Missing or invalid Authorization header");
                }

                // 2) Kiểm tra và trừ credit
                boolean hasCredit = creditService.deductCredit(userId);
                if (!hasCredit) {
                    Map<String, String> error = new HashMap<>();
                    error.put("type", "error");
                    error.put("message", "Bạn đã hết credit, vui lòng mua thêm gói để tiếp tục");
                    emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
                    emitter.complete();
                    return;
                }

                // 3) Lấy input và validate
                String prompt = Optional.ofNullable(body.get("prompt")).orElseThrow(() -> 
                    new IllegalArgumentException("Prompt is required"));
                String tone = Optional.ofNullable(body.get("tone")).orElse("medium");
                String sessionIdStr = Optional.ofNullable(body.get("session_id")).orElseThrow(() -> 
                    new IllegalArgumentException("Session ID is required"));

                // 4) Kiểm tra từ khóa bị cấm
                if (aiChatService.containsForbiddenKeyword(prompt)) {
                    String reply = aiChatService.getDefaultForbiddenReply();
                    Map<String, String> json = new HashMap<>();
                    json.put("type", "chunk");
                    json.put("role", "assistant");
                    json.put("content", reply);
                    emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);

                    Map<String, String> done = new HashMap<>();
                    done.put("type", "done");
                    emitter.send(objectMapper.writeValueAsString(done) + "\n", NDJSON);

                    UUID sessionId = UUID.fromString(sessionIdStr);
                    conversationLogService.saveLog(userId, prompt, reply, false, sessionId);
                    emitter.complete();
                    return;
                }

                // 5) Xây dựng prompt và gọi AI
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage("system", buildWritingSystemPrompt(tone)));
                messages.add(new ChatMessage("user", prompt));

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model("gpt-4o")
                        .messages(messages)
                        .temperature(0.7)
                        .stream(true)
                        .build();

                // 6) Xử lý stream
                openAiService.streamChatCompletion(request).doOnError(err -> {
                    try {
                        Map<String, String> error = new HashMap<>();
                        error.put("type", "error");
                        error.put("message", err.getMessage());
                        emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
                    } catch (Exception ignored) {}
                }).blockingForEach(chunk -> {
                    try {
                        var choices = chunk.getChoices();
                        if (choices == null || choices.isEmpty()) return;

                        String content = extractChoiceContent(choices.get(0));
                        if (content == null || content.isEmpty()) return;

                        fullResponse.append(content);
                        Map<String, String> json = new HashMap<>();
                        json.put("type", "chunk");
                        json.put("role", "assistant");
                        json.put("content", content);
                        emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);
                    } catch (Exception e) {
                        emitter.completeWithError(e);
                    }
                });

                // 7) Gửi tín hiệu kết thúc và số credit còn lại
                Map<String, Object> done = new HashMap<>();
                done.put("type", "done");
                done.put("remainingCredit", creditService.getRemainingCredit(userId));
                emitter.send(objectMapper.writeValueAsString(done) + "\n", NDJSON);

                // 8) Lưu log
                UUID sessionId = UUID.fromString(sessionIdStr);
                conversationLogService.saveLog(userId, prompt, fullResponse.toString(), false, sessionId);

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

    // System prompt cho viết văn
    private String buildWritingSystemPrompt(String tone) {
        String lengthGuidance;
        switch (tone) {
            case "long":
                lengthGuidance = "Viết bài dài khoảng 500-800 từ, chi tiết và đầy đủ.";
                break;
            case "outline":
                lengthGuidance = "Viết dàn ý chi tiết cho bài văn, bao gồm mở bài, thân bài, kết bài.";
                break;
            default:
                lengthGuidance = "Viết bài vừa phải, khoảng 300-500 từ, rõ ràng và mạch lạc.";
                break;
        }

        return String.join("\n",
            "Bạn là trợ lý viết văn tiếng Việt, chuyên hỗ trợ tạo nội dung sáng tạo và chất lượng.",
            "YÊU CẦU ĐỊNH DẠNG:", "- Sử dụng Markdown cho định dạng.", "- Không dùng HTML thô.",
            "PHONG CÁCH TRÌNH BÀY:", lengthGuidance,
            "- Ngôn ngữ tự nhiên, thân thiện, phù hợp với người Việt Nam.",
            "- Nếu là dàn ý, trình bày dưới dạng danh sách Markdown với các mục rõ ràng.",
            "- Gợi ý 3-5 câu hỏi hoặc định hướng thảo luận liên quan đến nội dung sau khi hoàn thành.");
    }

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