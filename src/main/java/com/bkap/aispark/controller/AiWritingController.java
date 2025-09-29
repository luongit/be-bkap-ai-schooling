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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api")
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
     *   "messages": [{ "role":"user"|"assistant"|"system", "content":"..." }, ...],
     *   "tone": "Trang trọng"|"Thân mật" (optional),
     *   "language": "Tiếng Việt"|"Tiếng Anh" (optional),
     *   "length": "Ngắn"|"Vừa"|"Dài"|"Mặc định" (optional),
     *   "session_id": "UUID"
     * }
     */
    @PostMapping(path = "/writing", produces = "application/x-ndjson")
    public ResponseBodyEmitter writingStream(@RequestBody Map<String, Object> body, HttpServletRequest httpRequest,
            HttpServletResponse resp) {
        // Headers chống buffer/nén
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

                // 2) Kiểm tra và trừ credit (có thể trừ nhiều hơn cho viết văn)
                boolean hasCredit = creditService.deductCredit(userId);
	            if (!hasCredit) {
	                Map<String, String> error = new HashMap<>();
	                error.put("type", "error");
	                error.put("message", "Bạn đã hết credit, vui lòng mua thêm gói để tiếp tục");
	                emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
	                emitter.complete();
	                return;
	            }


                // 3) Lấy input & tham số
                @SuppressWarnings("unchecked")
                List<Map<String, String>> messagesData = (List<Map<String, String>>) body.get("messages");
                String tone = Optional.ofNullable((String) body.get("tone")).orElse("Trang trọng");
                String language = Optional.ofNullable((String) body.get("language")).orElse("Tiếng Việt");
                String length = Optional.ofNullable((String) body.get("length")).orElse("Mặc định");

                String userMessage = messagesData != null && !messagesData.isEmpty()
                        ? messagesData.get(messagesData.size() - 1).get("content")
                        : "";

                // 4) Kiểm tra từ khóa bị cấm
                if (aiChatService.containsForbiddenKeyword(userMessage)) {
                    String reply = aiChatService.getDefaultForbiddenReply();
                    Map<String, String> json = new HashMap<>();
                    json.put("type", "chunk");
                    json.put("role", "assistant");
                    json.put("content", reply);
                    emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);

                    Map<String, String> done = new HashMap<>();
                    done.put("type", "done");
                    emitter.send(objectMapper.writeValueAsString(done) + "\n", NDJSON);

                    UUID sessionId = UUID.fromString(body.get("session_id").toString());
                    conversationLogService.saveLog(userId, userMessage, reply, false, sessionId);
                    emitter.complete();
                    return;
                }

                // 5) Xây dựng prompt và gọi AI
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage("system", buildWritingPrompt(tone, language, length)));

                if (messagesData != null) {
                    for (Map<String, String> m : messagesData) {
                        String role = m.get("role");
                        String content = m.get("content");
                        if (role != null && content != null) {
                            messages.add(new ChatMessage(role, content));
                        }
                    }
                }

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model("gpt-4o")
                        .messages(messages)
                        .temperature(0.3) // Nhiệt độ thấp hơn để đảm bảo văn phong chuẩn
                        .stream(true)
                        .build();

                // 6) Chuẩn hóa khi stream
                final StringBuilder safeBuffer = new StringBuilder();
                final LatexNormalizer normalizer = new LatexNormalizer();

                openAiService.streamChatCompletion(request).doOnError(err -> {
                    try {
                        Map<String, String> error = new HashMap<>();
                        error.put("type", "error");
                        error.put("message", err.getMessage());
                        emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
                    } catch (Exception ignored) {
                    }
                }).blockingForEach(chunk -> {
                    try {
                        var choices = chunk.getChoices();
                        if (choices == null || choices.isEmpty()) return;

                        String content = extractChoiceContent(choices.get(0));
                        if (content == null || content.isEmpty()) return;

                        safeBuffer.append(content);
                        fullResponse.append(content);
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

                // 7) Flush phần còn lại
                String tail = normalizer.flushAll(safeBuffer);
                if (!tail.isEmpty()) {
                    fullResponse.append(tail);
                    Map<String, String> json = new HashMap<>();
                    json.put("type", "chunk");
                    json.put("role", "assistant");
                    json.put("content", tail);
                    emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);
                }

                // 8) Gửi tín hiệu kết thúc và số credit còn lại
                Map<String, Object> done = new HashMap<>();
                done.put("type", "done");
                done.put("remainingCredit", creditService.getRemainingCredit(userId));
                emitter.send(objectMapper.writeValueAsString(done) + "\n", NDJSON);

                // 9) Lưu log
                UUID sessionId = UUID.fromString(body.get("session_id").toString());
                conversationLogService.saveLog(userId, userMessage, fullResponse.toString(), false, sessionId);

                emitter.complete();

            } catch (Exception e) {
                try {
                    Map<String, String> error = new HashMap<>();
                    error.put("type", "error");
                    error.put("message", e.getMessage());
                    emitter.send(objectMapper.writeValueAsString(error) + "\n", NDJSON);
                } catch (Exception ignored) {
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    // Prompt chuyên biệt cho viết văn
    private String buildWritingPrompt(String tone, String language, String length) {
        String toneInstruction;
        switch (tone) {
            case "Thân mật":
                toneInstruction = "Sử dụng giọng văn thân mật, gần gũi, như trò chuyện với bạn bè.";
                break;
            default:
                toneInstruction = "Sử dụng giọng văn trang trọng, mạch lạc, phù hợp với văn viết học thuật.";
                break;
        }

        String lengthInstruction;
        switch (length) {
            case "Ngắn":
                lengthInstruction = "Bài văn ngắn gọn, khoảng 200-300 từ.";
                break;
            case "Vừa":
                lengthInstruction = "Bài văn có độ dài trung bình, khoảng 400-600 từ.";
                break;
            case "Dài":
                lengthInstruction = "Bài văn chi tiết, khoảng 800-1000 từ.";
                break;
            default:
                lengthInstruction = "Bài văn có độ dài phù hợp với yêu cầu, khoảng 400-600 từ.";
                break;
        }

        String languageInstruction = language.equals("Tiếng Anh") 
            ? "Viết bài văn bằng tiếng Anh, đảm bảo ngữ pháp và từ vựng chính xác."
            : "Viết bài văn bằng tiếng Việt, sử dụng ngôn ngữ tự nhiên và đúng chuẩn mực.";

        return String.join("\n",
                "Bạn là một trợ lý AI chuyên viết văn tiếng Việt hoặc tiếng Anh, hỗ trợ học sinh, sinh viên trong việc sáng tác các bài văn theo yêu cầu.",
                "YÊU CẦU ĐỊNH DẠNG:",
                "- Bài văn phải có cấu trúc rõ ràng: mở bài, thân bài, kết bài.",
                "- Sử dụng Markdown để định dạng. Không dùng HTML thô.",
                "- Nếu có công thức toán học: inline dùng $...$, block dùng $$...$$ trên dòng riêng.",
                "- Không bọc công thức trong code fence ```...```.",
                "- Khi liệt kê, dùng danh sách hoặc bảng Markdown.",
                "- Ưu tiên ngôn ngữ tự nhiên, mạch lạc, đúng ngữ pháp.",
                "",
                "YÊU CẦU NỘI DUNG:",
                toneInstruction,
                languageInstruction,
                lengthInstruction,
                "- Nếu người dùng yêu cầu một loại văn cụ thể (phân tích, nghị luận, miêu tả, v.v.), tuân thủ đúng đặc điểm của loại văn đó.",
                "- Đảm bảo bài văn phù hợp với chương trình giáo dục Việt Nam (nếu bằng tiếng Việt).",
                "- Kết thúc bài văn, thêm 3-5 câu hỏi gợi ý để người dùng tự kiểm tra hoặc mở rộng tư duy."
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
        } catch (Exception ignore) {
        }

        try {
            var getMessage = choice.getClass().getMethod("getMessage");
            Object msg = getMessage.invoke(choice);
            if (msg != null) {
                var getContent = msg.getClass().getMethod("getContent");
                Object c = getContent.invoke(msg);
                if (c != null) return c.toString();
            }
        } catch (Exception ignore) {
        }

        try {
            var getText = choice.getClass().getMethod("getText");
            Object t = getText.invoke(choice);
            if (t != null) return t.toString();
        } catch (Exception ignore) {
        }

        return null;
    }
}