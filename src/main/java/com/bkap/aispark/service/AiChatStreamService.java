package com.bkap.aispark.service;

import com.bkap.aispark.dto.ChatMessageRequest;
import com.bkap.aispark.entity.AiAssistant;
import com.bkap.aispark.entity.AiConversation;
import com.bkap.aispark.entity.AiMessage;
import com.bkap.aispark.repository.AiAssistantDocumentRepository;
import com.bkap.aispark.repository.AiAssistantRepository;
import com.bkap.aispark.repository.AiConversationRepository;
import com.bkap.aispark.repository.AiMessageRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiChatStreamService {

	private final AiAssistantRepository assistantRepo;
	private final AiConversationRepository conversationRepo;
	private final AiMessageRepository messageRepo;
	private final OpenAiService openAiService;
	private final AiAssistantDocumentRepository docRepo;

	// Giới hạn knowledge gửi lên (tránh 50 trang => bắn token quá nhiều)
	private static final int MAX_KNOWLEDGE_CHARS = 20_000;

	public AiChatStreamService(AiAssistantRepository assistantRepo, AiConversationRepository conversationRepo,
			AiMessageRepository messageRepo, AiAssistantDocumentRepository docRepo, OpenAiService openAiService) {
		this.assistantRepo = assistantRepo;
		this.conversationRepo = conversationRepo;
		this.messageRepo = messageRepo;
		this.docRepo = docRepo;
		this.openAiService = openAiService;
	}

	public ResponseBodyEmitter handleStream(ChatMessageRequest req) {

		ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L); // 0 = không timeout

		try {
			AiConversation conversation = conversationRepo.findById(req.getConversationId())
					.orElseThrow(() -> new RuntimeException("Conversation not found"));

			AiAssistant assistant = conversation.getAssistant();

			
			var docs = docRepo.findByAssistantId(assistant.getId());

			String knowledge = docs.stream().map(d -> {
				String s = null;
				// Ưu tiên summary nếu có
				try {
					
					s = d.getSummaryText();
				} catch (Exception ignore) {
				}
				if (s != null && !s.isBlank())
					return s;

				// fallback raw_text
				try {
					return d.getRawText();
				} catch (Exception e) {
					return null;
				}
			}).filter(t -> t != null && !t.isBlank()).reduce((a, b) -> a + "\n\n" + b).orElse("");

			// cắt bớt nếu quá dài (đỡ tốn token + tránh vượt context)
			knowledge = clampChars(knowledge, MAX_KNOWLEDGE_CHARS);

			// ===== 1) System prompt DUY NHẤT (prompt gốc + knowledge) =====
			String systemPrompt = """
					%s

					=== KIẾN THỨC THAM KHẢO (từ tài liệu người dùng cung cấp) ===
					%s

					NGUYÊN TẮC TRẢ LỜI:
					- Nếu câu hỏi là kiến thức chung (ví dụ: các cung, các sao, nguyên lý), hãy trả lời trực tiếp từ tài liệu.
					- Nếu tài liệu chưa đủ, có thể suy luận thêm từ kiến thức chung, nhưng phải nói rõ đó là "phân tích bổ sung".
					- Trả lời rõ ràng, có cấu trúc, điềm đạm, không mê tín.
					"""
					.formatted(safe(assistant.getSystemPrompt()),
							knowledge.isBlank() ? "(Không có tài liệu tham khảo)" : knowledge);

			// ===== 2) Build messages: system + history + user =====
			List<ChatMessage> messages = new ArrayList<>();
			messages.add(new ChatMessage("system", systemPrompt));

			// history
			List<AiMessage> history = messageRepo.findAllByConversationOrderByCreatedAtAsc(conversation);
			for (AiMessage old : history) {
				// chỉ add các role hợp lệ
				String role = old.getRole();
				if (role == null)
					continue;
				role = role.trim().toLowerCase();
				if (!role.equals("user") && !role.equals("assistant") && !role.equals("system"))
					continue;

				String content = old.getContent();
				if (content == null || content.isBlank())
					continue;

				messages.add(new ChatMessage(role, content));
			}

			// user message (CỰC QUAN TRỌNG)
			String userText = safe(req.getMessage());
			messages.add(new ChatMessage("user", userText));

			// lưu user message DB
			messageRepo.save(AiMessage.builder().conversation(conversation).role("user").content(userText).build());

			// ===== 3) Streaming request =====
			ChatCompletionRequest request = ChatCompletionRequest.builder().model(assistant.getModel())
					.messages(messages).stream(true).temperature(0.7).build();

			StringBuilder fullAssistantText = new StringBuilder();

			openAiService.streamChatCompletion(request).doOnError(e -> {
				try {
					emitter.send("{\"event\":\"error\",\"message\":\"" + escape(e.getMessage()) + "\"}\n");
					emitter.completeWithError(e);
				} catch (Exception ignore) {
				}
			}).doOnComplete(() -> {
				try {
					// lưu assistant message DB
					messageRepo.save(AiMessage.builder().conversation(conversation).role("assistant")
							.content(fullAssistantText.toString()).build());

					emitter.send("{\"event\":\"done\"}\n");
					emitter.complete();
				} catch (Exception e) {
					emitter.completeWithError(e);
				}
			}).subscribe(res -> {
				try {
					if (res.getChoices() != null && !res.getChoices().isEmpty()) {
						// Theo thư viện theokanning: delta có thể nằm ở message.content
						String delta = null;
						if (res.getChoices().get(0).getMessage() != null) {
							delta = res.getChoices().get(0).getMessage().getContent();
						}
						if (delta != null && !delta.isEmpty()) {
							fullAssistantText.append(delta);
							String line = "{\"event\":\"delta\",\"text\":\"" + escape(delta) + "\"}\n";
							emitter.send(line);
						}
					}
				} catch (Exception e) {
					emitter.completeWithError(e);
				}
			});

		} catch (Exception e) {
			try {
				emitter.send("{\"event\":\"error\",\"message\":\"" + escape(e.getMessage()) + "\"}\n");
				emitter.completeWithError(e);
			} catch (Exception ignore) {
			}
		}

		return emitter;
	}

	// ===== Helpers =====

	private static String clampChars(String s, int maxChars) {
		if (s == null)
			return "";
		if (s.length() <= maxChars)
			return s;
		// lấy phần cuối để giữ phần “mới nhất” nếu bạn hay append
		// hoặc lấy đầu, tùy bạn. Ở đây lấy đầu.
		return s.substring(0, maxChars) + "\n\n[...ĐÃ RÚT GỌN VÌ TÀI LIỆU QUÁ DÀI...]";
	}

	private static String safe(String s) {
		return s == null ? "" : s;
	}

	private String escape(String text) {
		if (text == null)
			return "";
		return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
	}
}
