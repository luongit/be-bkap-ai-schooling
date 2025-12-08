package com.bkap.aispark.api;

import com.bkap.aispark.dto.ChatMessageRequest;
import com.bkap.aispark.entity.AiMessage;
import com.bkap.aispark.service.AiChatStreamService;
import com.bkap.aispark.service.AiMessageService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

@RestController
@RequestMapping("/api/chat")
public class AiChatApi {

    private final AiChatStreamService aiChatStreamService;

    @Autowired
    public AiChatApi(AiChatStreamService aiChatStreamService) {
        this.aiChatStreamService = aiChatStreamService;
    }
    
    @Autowired
    private AiMessageService messageService;

    // API to handle streaming chat messages
    @PostMapping("/stream")
    public ResponseEntity<ResponseBodyEmitter> streamChat(@RequestBody ChatMessageRequest req) {
        ResponseBodyEmitter emitter = aiChatStreamService.handleStream(req);
        return ResponseEntity.ok(emitter);
    }
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<List<AiMessage>> getMessagesByConversation(
            @PathVariable Long conversationId) {
        List<AiMessage> messages = messageService.getMessagesByConversationId(conversationId);
        return ResponseEntity.ok(messages);
    }
}
