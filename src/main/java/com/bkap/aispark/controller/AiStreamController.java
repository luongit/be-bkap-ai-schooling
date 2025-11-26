package com.bkap.aispark.controller;

import com.bkap.aispark.helper.LatexNormalizer;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.AiChatService;
import com.bkap.aispark.service.ConversationLogService;
import com.bkap.aispark.service.CreditService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.*;z

@RestController
@RequestMapping("/api")
public class AiStreamController {

    @Autowired
    private OpenAiService openAiService;

    @Autowired
    private ConversationLogService conversationLogService;

    @Autowired
    private JwtUtil jwtutil;

    @Autowired
    private AiChatService aiChatService;

    @Autowired
    private CreditService creditService;

    private static final MediaType NDJSON = MediaType.valueOf("application/x-ndjson");
    private final ObjectMapper objectMapper = new ObjectMapper();


    // ===================== API STREAM =====================
    @SuppressWarnings("unchecked")
    @PostMapping(path = "/stream", produces = "application/x-ndjson")
    public ResponseBodyEmitter stream(@RequestBody Map<String, Object> body,
                                      HttpServletRequest httpRequest,
                                      HttpServletResponse resp) {

        resp.setHeader("Content-Type", "application/x-ndjson; charset=utf-8");
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("X-Accel-Buffering", "no");

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L);

        // chạy async → không block request
        startStreamAsync(body, httpRequest, emitter);

        return emitter;
    }


    @Async("streamExecutor")  // dùng threadpool mới
    public void startStreamAsync(Map<String, Object> body,
                                 HttpServletRequest httpRequest,
                                 ResponseBodyEmitter emitter) {

        final StringBuilder fullResponse = new StringBuilder();

        try {
            // ---------- 1. Lấy User ID ----------
            String authHeader = httpRequest.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                throw new RuntimeException("Missing Authorization header");

            Long userId = jwtutil.getUserId(authHeader.substring(7));

            // ---------- 2. Lấy message người dùng ----------
            List<Map<String, String>> messagesData =
                    (List<Map<String, String>>) body.get("messages");

            String userMessage = messagesData != null && !messagesData.isEmpty()
                    ? messagesData.get(messagesData.size() - 1).get("content")
                    : "";

            // ---------- 3. Build prompt ----------
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", buildSystemPrompt("student")));

            for (Map<String, String> m : messagesData) {
                messages.add(new ChatMessage(m.get("role"), m.get("content")));
            }

            // ---------- 4. Gọi OpenAI (non-stream để lấy usage) ----------
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model("gpt-4o")
                    .messages(messages)
                    .temperature(0.2)
                    .stream(false)  // giữ nguyên logic
                    .build();

            var result = openAiService.createChatCompletion(request);
            var choice = result.getChoices().get(0);

            String reply = choice.getMessage().getContent();
            fullResponse.append(reply);

            int totalTokens = Optional.ofNullable(result.getUsage())
                    .map(u -> u.getCompletionTokens())
                    .map(Long::intValue)
                    .orElse(0);

            // ---------- 5. Trừ credit ----------
            String actionCode = (String) body.getOrDefault("actionCode", "CHAT_AI");

            boolean ok = creditService.deductByTokenUsage(
                    userId,
                    actionCode,
                    totalTokens,
                    "session-" + body.get("session_id")
            );

            if (!ok) {
                emitter.send(objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", "Không đủ credit để chat với AI!"
                )) + "\n", NDJSON);
                emitter.complete();
                return;
            }

            // ---------- 6. Gửi reply về FE ----------
            emitter.send(objectMapper.writeValueAsString(Map.of(
                    "type", "chunk",
                    "role", "assistant",
                    "content", reply
            )) + "\n", NDJSON);

            emitter.send(objectMapper.writeValueAsString(Map.of(
                    "type", "done",
                    "remainingCredit", creditService.getRemainingCredit(userId)
            )) + "\n", NDJSON);

            // ---------- 7. Lưu log ----------
            UUID sessionId = UUID.fromString(body.get("session_id").toString());
            conversationLogService.saveLog(userId, userMessage, reply, false, sessionId);

            emitter.complete();

        } catch (Exception e) {
            try {
                emitter.send(objectMapper.writeValueAsString(Map.of(
                        "type", "error",
                        "message", e.getMessage()
                )) + "\n", NDJSON);
            } catch (Exception ignored) {}
            emitter.completeWithError(e);
        }
    }


 // Prompt hệ thống
 	private String buildSystemPrompt(String audience) {
 		String tone;
 		switch (audience) {
 			case "kid":
 				tone = "Giải thích thật dễ hiểu, ví dụ gần gũi, câu ngắn, dùng emoji tiết chế 👦👧.";
 				break;
 			case "student":
 				tone = "Ngắn gọn, đi thẳng ý, có ví dụ và bài tập nhỏ.";
 				break;
 			case "teacher":
 				tone = "Chuẩn xác, có định nghĩa, tính chất, ví dụ mẫu và gợi ý chấm điểm.";
 				break;
 			default:
 				tone = "Thân thiện, rõ ràng, có ví dụ minh hoạ.";
 				break;
 		}

 		return String.join("\n", "Bạn là trợ lý học tập tiếng Việt phục vụ học sinh, sinh viên, trẻ nhỏ và giáo viên.",
 				"YÊU CẦU ĐỊNH DẠNG và CHUẨN HOÁ XUẤT RA:", "- Dùng Markdown. Không dùng HTML thô.",
 				"- Công thức toán học: inline dùng $...$, block dùng $$...$$. Không dùng \\( ... \\) hoặc \\[ ... \\].",
 				"- Với block math, đặt công thức trên dòng riêng giữa hai dấu $$.",
 				"- Không bọc công thức trong code fence ```...```.",
 				"- Khi cần liệt kê, dùng danh sách Markdown hoặc bảng Markdown.",
 				"- Nếu viết ký hiệu như pi, dùng \\pi; phân số dùng \\frac{...}{...}; tích dùng \\times; chia dùng \\div.",
 				"- Khi nêu công thức kèm điều kiện (ví dụ b ≠ 0) dùng inline math: $(b \\ne 0)$.", "",
 				"PHONG CÁCH TRÌNH BÀY:", tone,
 				"- Khi trả lời theo chương trình Bộ GD&ĐT Việt Nam, dùng thuật ngữ chuẩn, ví dụ: 'Trung bình cộng', 'BCNN', 'ƯCLN', ...",
 				"- Ưu tiên tính chính xác, ngắn gọn. Khi cần, thêm ví dụ minh hoạ ngắn.",
 				"- Luôn gợi ý 3 - 5 cấu hỏi hoặc đề xuất định hướng gợi ý phù hợp với nội dung mà người dùng đang hỏi",
 				"");
 	}

 	// Tương thích nhiều phiên bản theokanning
 	private String extractChoiceContent(Object choice) {
 		if (choice == null)
 			return null;
 		try {
 			var getDelta = choice.getClass().getMethod("getDelta");
 			Object delta = getDelta.invoke(choice);
 			if (delta != null) {
 				var getContent = delta.getClass().getMethod("getContent");
 				Object c = getContent.invoke(delta);
 				if (c != null)
 					return c.toString();
 			}
 		} catch (NoSuchMethodException ignore) {
 		} catch (Exception ignore) {
 		}

 		try {
 			var getMessage = choice.getClass().getMethod("getMessage");
 			Object msg = getMessage.invoke(choice);
 			if (msg != null) {
 				var getContent = msg.getClass().getMethod("getContent");
 				Object c = getContent.invoke(msg);
 				if (c != null)
 					return c.toString();
 			}
 		} catch (Exception ignore) {
 		}

 		try {
 			var getText = choice.getClass().getMethod("getText");
 			Object t = getText.invoke(choice);
 			if (t != null)
 				return t.toString();
 		} catch (Exception ignore) {
 		}

 		return null;
 	}
 }


