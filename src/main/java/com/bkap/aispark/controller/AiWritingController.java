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
     *   "tone": "Trang trọng"|"Thân mật"|"Sáng tạo"|"Chuyên nghiệp" (optional),
     *   "language": "Tiếng Việt"|"Tiếng Anh" (optional),
     *   "length": "Ngắn"|"Vừa"|"Dài"|"Rất dài"|"Mặc định" (optional),
     *   "writing_type": "Nghị luận"|"Miêu tả"|"Tự sự"|"Thuyết minh"|"Phân tích"|"Sáng tạo"|"Luận văn"|"Báo cáo" (optional),
     *   "education_level": "Cấp 1"|"Cấp 2"|"Cấp 3"|"Đại học"|"Sau đại học" (optional),
     *   "creativity": 0.0-1.0 (optional, mặc định 0.3),
     *   "include_outline": true|false (optional, mặc định false),
     *   "include_references": true|false (optional, mặc định false),
     *   "writing_style": "Văn học"|"Khoa học"|"Báo chí"|"Học thuật"|"Marketing" (optional),
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

                // 3) Lấy input & tham số nâng cao
                @SuppressWarnings("unchecked")
                List<Map<String, String>> messagesData = (List<Map<String, String>>) body.get("messages");
                
                String tone = Optional.ofNullable((String) body.get("tone")).orElse("Trang trọng");
                String language = Optional.ofNullable((String) body.get("language")).orElse("Tiếng Việt");
                String length = Optional.ofNullable((String) body.get("length")).orElse("Mặc định");
                String writingType = Optional.ofNullable((String) body.get("writing_type")).orElse("Tổng hợp");
                String educationLevel = Optional.ofNullable((String) body.get("education_level")).orElse("Cấp 3");
                String writingStyle = Optional.ofNullable((String) body.get("writing_style")).orElse("Học thuật");
                
                double creativity = Optional.ofNullable(body.get("creativity"))
                    .map(obj -> obj instanceof Number ? ((Number) obj).doubleValue() : 0.3)
                    .orElse(0.3);
                
                boolean includeOutline = Optional.ofNullable((Boolean) body.get("include_outline")).orElse(false);
                boolean includeReferences = Optional.ofNullable((Boolean) body.get("include_references")).orElse(false);

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

                // 5) Xây dựng prompt nâng cao
                List<ChatMessage> messages = new ArrayList<>();
                messages.add(new ChatMessage("system", buildAdvancedWritingPrompt(
                    tone, language, length, writingType, educationLevel, 
                    writingStyle, includeOutline, includeReferences
                )));

                if (messagesData != null) {
                    for (Map<String, String> m : messagesData) {
                        String role = m.get("role");
                        String content = m.get("content");
                        if (role != null && content != null) {
                            messages.add(new ChatMessage(role, content));
                        }
                    }
                }

                // 6) Tính toán temperature dựa trên creativity
                double temperature = Math.min(Math.max(creativity, 0.1), 1.0);

                ChatCompletionRequest request = ChatCompletionRequest.builder()
                        .model("gpt-4o")
                        .messages(messages)
                        .temperature(temperature)
                        .topP(0.95)
                        .stream(true)
                        .build();

                // 7) Chuẩn hóa khi stream
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

                // 8) Flush phần còn lại
                String tail = normalizer.flushAll(safeBuffer);
                if (!tail.isEmpty()) {
                    fullResponse.append(tail);
                    Map<String, String> json = new HashMap<>();
                    json.put("type", "chunk");
                    json.put("role", "assistant");
                    json.put("content", tail);
                    emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);
                }
                int wordCount = fullResponse.toString().split("\\s+").length;
                int minWords = 0;
                if ("Dài".equals(length)) {
                    minWords = 2000;
                } else if ("Rất dài".equals(length)) {
                    minWords = 3000;
                }

                if (minWords > 0 && wordCount < minWords) {
                    // Yêu cầu GPT viết tiếp
                    List<ChatMessage> continueMessages = new ArrayList<>();
                    continueMessages.add(new ChatMessage("system",
                        "Tiếp tục mở rộng bài viết trước đó. Hãy viết thêm để bài đạt ít nhất " 
                        + minWords + " từ. Không lặp lại, chỉ bổ sung thêm chi tiết, ví dụ, phân tích."));

                    continueMessages.add(new ChatMessage("assistant", fullResponse.toString()));

                    ChatCompletionRequest continueRequest = ChatCompletionRequest.builder()
                            .model("gpt-4o")
                            .messages(continueMessages)
                            .temperature(temperature)
                            .topP(0.95)
                            .stream(true)
                            .build();

                    final StringBuilder safeBuffer2 = new StringBuilder();

                    openAiService.streamChatCompletion(continueRequest).blockingForEach(chunk -> {
                        try {
                            var choices = chunk.getChoices();
                            if (choices == null || choices.isEmpty()) return;

                            String content = extractChoiceContent(choices.get(0));
                            if (content == null || content.isEmpty()) return;

                            safeBuffer2.append(content);
                            fullResponse.append(content);
                            String emitChunk = normalizer.tryNormalizeAndExtractStablePrefix(safeBuffer2);
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

                    // flush lần cuối
                    String tail2 = normalizer.flushAll(safeBuffer2);
                    if (!tail2.isEmpty()) {
                        fullResponse.append(tail2);
                        Map<String, String> json = new HashMap<>();
                        json.put("type", "chunk");
                        json.put("role", "assistant");
                        json.put("content", tail2);
                        emitter.send(objectMapper.writeValueAsString(json) + "\n", NDJSON);
                    }
                }

                // 9) Gửi tín hiệu kết thúc và số credit còn lại
                Map<String, Object> done = new HashMap<>();
                done.put("type", "done");
                done.put("remainingCredit", creditService.getRemainingCredit(userId));
                emitter.send(objectMapper.writeValueAsString(done) + "\n", NDJSON);

                // 10) Lưu log
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

    /**
     * Prompt nâng cao với nhiều tùy chọn
     */
    private String buildAdvancedWritingPrompt(String tone, String language, String length, 
            String writingType, String educationLevel, String writingStyle,
            boolean includeOutline, boolean includeReferences) {
        
        StringBuilder prompt = new StringBuilder();
        
        // Phần giới thiệu vai trò
        prompt.append("Bạn là một trợ lý AI chuyên nghiệp trong lĩnh vực viết văn, với khả năng sáng tác ");
        prompt.append("các bài văn chất lượng cao phù hợp với nhiều mục đích và đối tượng khác nhau.\n\n");
        
        // Yêu cầu định dạng
        prompt.append("═══ YÊU CẦU ĐỊNH DẠNG ═══\n");
        prompt.append("- Sử dụng Markdown để định dạng văn bản chuyên nghiệp\n");
        prompt.append("- Công thức toán học: inline dùng $...$, block dùng $$...$$\n");
        prompt.append("- Tiêu đề: dùng #, ##, ### cho các cấp độ\n");
        prompt.append("- Danh sách: dùng -, *, hoặc 1., 2., 3.\n");
        prompt.append("- Nhấn mạnh: **in đậm**, *in nghiêng*, ***in đậm nghiêng***\n");
        prompt.append("- Trích dẫn: dùng > cho blockquote\n");
        prompt.append("- Bảng: dùng cú pháp bảng Markdown khi cần thiết\n");
        prompt.append("- KHÔNG bao giờ dùng HTML thô hoặc code fence cho công thức\n\n");
        
        // Cấu trúc bài viết
        prompt.append("═══ CẤU TRÚC BÀI VIẾT ═══\n");
        
        if (includeOutline) {
            prompt.append("1. **Dàn ý** (ngắn gọn, 3-5 ý chính)\n");
            prompt.append("2. **Mở bài** - Thu hút người đọc, đặt vấn đề\n");
            prompt.append("3. **Thân bài** - Triển khai nội dung theo dàn ý\n");
            prompt.append("4. **Kết bài** - Tổng kết, đúc kết, khẳng định quan điểm\n");
            if (includeReferences) {
                prompt.append("5. **Tham khảo** - Danh sách nguồn tham khảo (nếu cần)\n");
            }
            prompt.append("6. **Câu hỏi mở rộng** - 3-5 câu hỏi để tư duy thêm\n\n");
        } else {
            prompt.append("- Mở bài: Thu hút, đặt vấn đề rõ ràng\n");
            prompt.append("- Thân bài: Triển khai logic, có dẫn chứng cụ thể\n");
            prompt.append("- Kết bài: Khẳng định, tổng kết, mở rộng\n");
            prompt.append("- Câu hỏi mở rộng: 3-5 câu cuối bài\n\n");
        }
        
        // Loại văn bản
        prompt.append("═══ LOẠI VĂN BẢN ═══\n");
        prompt.append(getWritingTypeInstructions(writingType));
        prompt.append("\n");
        
        // Giọng văn
        prompt.append("═══ GIỌNG VĂN ═══\n");
        prompt.append(getToneInstructions(tone));
        prompt.append("\n");
        
        // Phong cách viết
        prompt.append("═══ PHONG CÁCH ═══\n");
        prompt.append(getStyleInstructions(writingStyle));
        prompt.append("\n");
        
        // Độ dài
        prompt.append("═══ ĐỘ DÀI ═══\n");
        prompt.append(getLengthInstructions(length));
        prompt.append("\n");
        
        // Trình độ
        prompt.append("═══ TRÌNH ĐỘ ═══\n");
        prompt.append(getEducationLevelInstructions(educationLevel));
        prompt.append("\n");
        
        // Ngôn ngữ
        prompt.append("═══ NGÔN NGỮ ═══\n");
        if (language.equals("Tiếng Anh")) {
            prompt.append("- Viết bằng tiếng Anh chuẩn (American hoặc British)\n");
            prompt.append("- Đảm bảo ngữ pháp, chính tả hoàn hảo\n");
            prompt.append("- Sử dụng từ vựng phong phú, phù hợp ngữ cảnh\n");
            prompt.append("- Câu văn mạch lạc, liền mạch\n");
        } else {
            prompt.append("- Viết bằng tiếng Việt chuẩn mực, tự nhiên\n");
            prompt.append("- Tuân thủ chính tả, ngữ pháp tiếng Việt\n");
            prompt.append("- Sử dụng từ ngữ phong phú, hạn chế lặp từ\n");
            prompt.append("- Câu văn trôi chảy, có nhịp điệu\n");
        }
        prompt.append("\n");
        
        // Yêu cầu chất lượng
        prompt.append("═══ YÊU CẦU CHẤT LƯỢNG ═══\n");
        prompt.append("✓ Nội dung chính xác, có căn cứ\n");
        prompt.append("✓ Lập luận chặt chẽ, logic rõ ràng\n");
        prompt.append("✓ Dẫn chứng cụ thể, thuyết phục\n");
        prompt.append("✓ Ngôn ngữ phù hợp với đối tượng độc giả\n");
        prompt.append("✓ Tránh sai sót chính tả, ngữ pháp\n");
        prompt.append("✓ Có chiều sâu tư tưởng, không hời hợt\n");
        prompt.append("✓ Sáng tạo nhưng không xa rời thực tế\n");
        prompt.append("✓ Tôn trọng giá trị văn hóa, đạo đức\n\n");
        
        // Lưu ý đặc biệt
        prompt.append("═══ LƯU Ý ═══\n");
        prompt.append("- Phân tích kỹ yêu cầu của người dùng trước khi viết\n");
        prompt.append("- Điều chỉnh phong cách phù hợp với mục đích sử dụng\n");
        prompt.append("- Đảm bảo tính nhất quán trong toàn bài\n");
        prompt.append("- Kiểm tra lại trước khi hoàn thiện\n");
        
        return prompt.toString();
    }

    private String getWritingTypeInstructions(String type) {
        switch (type) {
            case "Nghị luận":
                return "**Văn nghị luận xã hội/văn học:**\n" +
                       "- Đặt vấn đề rõ ràng, có tính thời sự\n" +
                       "- Phân tích đa chiều, nhiều góc độ\n" +
                       "- Dẫn chứng từ thực tế, văn học, lịch sử\n" +
                       "- Lập luận chặt chẽ, khách quan\n" +
                       "- Kết luận có tính khẳng định, truyền cảm hứng";
            
            case "Miêu tả":
                return "**Văn miêu tả:**\n" +
                       "- Tập trung vào chi tiết cảm quan (thị giác, thính giác, khứu giác, xúc giác)\n" +
                       "- Sử dụng từ ngữ sinh động, hình ảnh cụ thể\n" +
                       "- Kết hợp tu từ: so sánh, ẩn dụ, nhân hóa\n" +
                       "- Tạo không gian, thời gian rõ ràng\n" +
                       "- Truyền tải cảm xúc qua hình ảnh";
            
            case "Tự sự":
                return "**Văn tự sự:**\n" +
                       "- Kể chuyện theo trình tự thời gian hoặc logic\n" +
                       "- Có nhân vật, sự kiện, tình tiết rõ ràng\n" +
                       "- Kết hợp miêu tả, biểu cảm\n" +
                       "- Tạo điểm nhấn, cao trào trong câu chuyện\n" +
                       "- Bài học, ý nghĩa sâu sắc";
            
            case "Thuyết minh":
                return "**Văn thuyết minh:**\n" +
                       "- Giải thích rõ ràng về đối tượng/vấn đề\n" +
                       "- Cung cấp thông tin chính xác, khách quan\n" +
                       "- Sử dụng số liệu, dẫn chứng khoa học\n" +
                       "- Trình bày có hệ thống, logic\n" +
                       "- Ngôn ngữ súc tích, dễ hiểu";
            
            case "Phân tích":
                return "**Văn phân tích:**\n" +
                       "- Đi sâu vào bản chất vấn đề\n" +
                       "- Phân tích nguyên nhân, hệ quả\n" +
                       "- So sánh, đối chiếu nhiều yếu tố\n" +
                       "- Đánh giá khách quan, có căn cứ\n" +
                       "- Rút ra kết luận có giá trị";
            
            case "Sáng tạo":
                return "**Văn sáng tạo (truyện ngắn, thơ, kịch):**\n" +
                       "- Phát huy trí tưởng tượng, sáng tạo\n" +
                       "- Xây dựng nhân vật, bối cảnh độc đáo\n" +
                       "- Sử dụng nghệ thuật ngôn từ cao\n" +
                       "- Tạo điểm nhấn nghệ thuật\n" +
                       "- Truyền tải thông điệp nhân văn";
            
            case "Luận văn":
                return "**Luận văn khoa học:**\n" +
                       "- Tuân thủ cấu trúc: Đặt vấn đề - Giải quyết - Kết luận\n" +
                       "- Tổng quan nghiên cứu có hệ thống\n" +
                       "- Phương pháp nghiên cứu rõ ràng\n" +
                       "- Dẫn nguồn tài liệu chính xác\n" +
                       "- Ngôn ngữ học thuật, khách quan";
            
            case "Báo cáo":
                return "**Báo cáo công việc/nghiên cứu:**\n" +
                       "- Cấu trúc rõ ràng: Mục đích - Nội dung - Kết quả\n" +
                       "- Trình bày súc tích, đi thẳng vào vấn đề\n" +
                       "- Sử dụng biểu đồ, bảng số liệu\n" +
                       "- Đánh giá, đề xuất cụ thể\n" +
                       "- Ngôn ngữ trang trọng, chuyên nghiệp";
            
            default:
                return "**Tổng hợp:**\n" +
                       "- Linh hoạt theo yêu cầu người dùng\n" +
                       "- Kết hợp nhiều thể loại khi cần\n" +
                       "- Đảm bảo tính logic và mạch lạc\n" +
                       "- Phù hợp với mục đích sử dụng";
        }
    }

    private String getToneInstructions(String tone) {
        switch (tone) {
            case "Thân mật":
                return "- Giọng văn gần gũi, như trò chuyện với bạn bè\n" +
                       "- Sử dụng đại từ nhân xưng thân mật (mình, bạn, chúng ta)\n" +
                       "- Câu văn ngắn, dễ hiểu, tự nhiên\n" +
                       "- Có thể dùng biểu cảm nhẹ nhàng";
            
            case "Sáng tạo":
                return "- Giọng văn độc đáo, có cá tính\n" +
                       "- Sử dụng nghệ thuật tu từ phong phú\n" +
                       "- Câu văn có nhịp điệu, hình ảnh ấn tượng\n" +
                       "- Táo bạo trong cách diễn đạt";
            
            case "Chuyên nghiệp":
                return "- Giọng văn trang trọng nhưng không cứng nhắc\n" +
                       "- Sử dụng thuật ngữ chuyên môn chính xác\n" +
                       "- Câu văn rõ ràng, súc tích\n" +
                       "- Tôn trọng chuẩn mực viết lách";
            
            default: // Trang trọng
                return "- Giọng văn trang trọng, học thuật\n" +
                       "- Sử dụng ngôn ngữ chuẩn mực, trang nhã\n" +
                       "- Câu văn mạch lạc, có chiều sâu\n" +
                       "- Thể hiện sự tôn trọng với người đọc";
        }
    }

    private String getStyleInstructions(String style) {
        switch (style) {
            case "Văn học":
                return "- Chú trọng nghệ thuật ngôn từ\n" +
                       "- Sử dụng hình ảnh, biểu tượng\n" +
                       "- Tạo không gian cảm xúc\n" +
                       "- Có giá trị thẩm mỹ cao";
            
            case "Khoa học":
                return "- Chính xác, khách quan\n" +
                       "- Sử dụng dữ liệu, số liệu\n" +
                       "- Lập luận logic, có căn cứ\n" +
                       "- Thuật ngữ chuyên môn chuẩn xác";
            
            case "Báo chí":
                return "- Ngắn gọn, súc tích\n" +
                       "- Thông tin rõ ràng, cập nhật\n" +
                       "- Câu văn dễ đọc, dễ hiểu\n" +
                       "- Thu hút sự chú ý người đọc";
            
            case "Marketing":
                return "- Hấp dẫn, thuyết phục\n" +
                       "- Tập trung vào lợi ích người đọc\n" +
                       "- Sử dụng ngôn ngữ tích cực\n" +
                       "- Có lời kêu gọi hành động";
            
            default: // Học thuật
                return "- Tuân thủ chuẩn mực học thuật\n" +
                       "- Dẫn nguồn, tham khảo rõ ràng\n" +
                       "- Phân tích sâu, có hệ thống\n" +
                       "- Khách quan, có giá trị nghiên cứu";
        }
    }

    private String getLengthInstructions(String length) {
        switch (length) {
            case "Ngắn":
                return "- Độ dài: 200-400 từ\n" +
                       "- Tập trung vào ý chính\n" +
                       "- Loại bỏ thông tin thừa\n" +
                       "- Đi thẳng vào vấn đề";
            
            case "Vừa":
                return "- Độ dài: 800-1000 từ\n" +
                       "- Phát triển ý đầy đủ\n" +
                       "- Có dẫn chứng, ví dụ cụ thể\n" +
                       "- Cân bằng giữa nội dung và hình thức";
            
            case "Dài":
                return "- Độ dài: 2000-3500 từ\n" +
                       "- Triển khai chi tiết, sâu sắc\n" +
                       "- Nhiều luận điểm, dẫn chứng\n" +
                       "- Phân tích đa chiều vấn đề";
            
            case "Rất dài":
                return "- Độ dài: 3500-4500 từ hoặc hơn\n" +
                       "- Nghiên cứu chuyên sâu\n" +
                       "- Tổng quan toàn diện\n" +
                       "- Phù hợp cho luận văn, báo cáo lớn";
            
            default:
                return "- Độ dài: 500-700 từ (linh hoạt theo nội dung)\n" +
                       "- Đảm bảo đủ để triển khai ý\n" +
                       "- Không quá dài gây nhàm chán\n" +
                       "- Phù hợp với yêu cầu cụ thể";
        }
    }

    private String getEducationLevelInstructions(String level) {
        switch (level) {
            case "Cấp 1":
                return "- Ngôn ngữ đơn giản, dễ hiểu\n" +
                       "- Câu văn ngắn, rõ ràng\n" +
                       "- Hình ảnh gần gũi, quen thuộc\n" +
                       "- Phù hợp với học sinh tiểu học";
            
            case "Cấp 2":
                return "- Ngôn ngữ phong phú hơn\n" +
                       "- Câu văn có độ phức tạp vừa phải\n" +
                       "- Bắt đầu sử dụng tu từ cơ bản\n" +
                       "- Phù hợp với học sinh THCS";
            
            case "Cấp 3":
                return "- Ngôn ngữ học thuật ban đầu\n" +
                       "- Câu văn phức, có chiều sâu\n" +
                       "- Sử dụng tu từ, dẫn chứng đa dạng\n" +
                       "- Phù hợp với học sinh THPT";
            
            case "Đại học":
                return "- Ngôn ngữ chuyên môn, học thuật\n" +
                       "- Phân tích sâu, có hệ thống\n" +
                       "- Dẫn nguồn, tham khảo chính xác\n" +
                       "- Tư duy phản biện, độc lập";
            
            case "Sau đại học":
                return "- Ngôn ngữ học thuật cao cấp\n" +
                       "- Nghiên cứu chuyên sâu, đổi mới\n" +
                       "- Phương pháp luận chặt chẽ\n" +
                       "- Đóng góp tri thức mới";
            
            default:
                return "- Ngôn ngữ phù hợp với trình độ THPT\n" +
                       "- Cân bằng giữa học thuật và dễ hiểu\n" +
                       "- Phát triển tư duy logic\n" +
                       "- Chuẩn bị cho bậc học cao hơn";
        }
    }

    /**
     * Tương thích nhiều phiên bản theokanning
     */
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