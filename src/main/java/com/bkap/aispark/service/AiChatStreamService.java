package com.bkap.aispark.service;

import com.bkap.aispark.dto.ChatMessageRequest;
import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import com.theokanning.openai.completion.chat.ChatCompletionChunk;
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

    public AiChatStreamService(AiAssistantRepository assistantRepo,
                               AiConversationRepository conversationRepo,
                               AiMessageRepository messageRepo,
                               OpenAiService openAiService) {
        this.assistantRepo = assistantRepo;
        this.conversationRepo = conversationRepo;
        this.messageRepo = messageRepo;
        this.openAiService = openAiService;
    }

    public ResponseBodyEmitter handleStream(ChatMessageRequest req) {

        ResponseBodyEmitter emitter = new ResponseBodyEmitter(0L); // 0 = không timeout

        try {
            AiConversation conversation =
                    conversationRepo.findById(req.getConversationId())
                            .orElseThrow(() -> new RuntimeException("Conversation not found"));

            AiAssistant assistant = conversation.getAssistant();

            // 1. Build messages (system + history + user mới)
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage("system", assistant.getSystemPrompt()));

            List<AiMessage> history = messageRepo.findAllByConversationOrderByCreatedAtAsc(conversation);
            for (AiMessage old : history) {
                messages.add(new ChatMessage(old.getRole(), old.getContent()));
            }

            // user message
            messages.add(new ChatMessage("user", req.getMessage()));

            // Lưu user message vào DB
            AiMessage userMsg = AiMessage.builder()
                    .conversation(conversation)
                    .role("user")
                    .content(req.getMessage())
                    .build();
            messageRepo.save(userMsg);

            // 2. Streaming request
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(assistant.getModel())
                    .messages(messages)
                    .stream(true)
                    .temperature(0.7)
                    .build();

            StringBuilder fullAssistantText = new StringBuilder();

            openAiService.streamChatCompletion(request)
                    .doOnError(e -> {
                        try {
                            emitter.send("{\"event\":\"error\",\"message\":\"" + escape(e.getMessage()) + "\"}\n");
                            emitter.completeWithError(e);
                        } catch (Exception ignore) {}
                    })
                    .doOnComplete(() -> {
                        try {
                            // Lưu full assistant message khi stream xong
                            AiMessage assistantMsg = AiMessage.builder()
                                    .conversation(conversation)
                                    .role("assistant")
                                    .content(fullAssistantText.toString())
                                    .build();
                            messageRepo.save(assistantMsg);

                            emitter.send("{\"event\":\"done\"}\n");
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    })
                    .subscribe(res -> {
                        try {
                            // Kiểm tra Choices và trả về content trong message
                            if (res.getChoices() != null && !res.getChoices().isEmpty()) {
                                String delta = res.getChoices().get(0).getMessage().getContent(); // <-- sửa ở đây
                                if (delta != null && !delta.isEmpty()) {
                                    fullAssistantText.append(delta);
                                    String line = "{\"event\":\"delta\",\"text\":\"" + escape(delta) + "\"}\n";
                                    emitter.send(line); // Send token to frontend
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
            } catch (Exception ignore) {}
        }

        return emitter;
    }

    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
