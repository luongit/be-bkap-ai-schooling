package com.bkap.aispark.api;

import com.bkap.aispark.dto.ConversationCreateRequest;
import com.bkap.aispark.entity.AiConversation;
import com.bkap.aispark.service.AiConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations/chatbot")
public class AiConversationApi {

    private final AiConversationService aiConversationService;

    @Autowired
    public AiConversationApi(AiConversationService aiConversationService) {
        this.aiConversationService = aiConversationService;
    }

    // API to create a new conversation
    @PostMapping
    public ResponseEntity<AiConversation> createConversation(@RequestBody ConversationCreateRequest dto) {
        AiConversation conversation = aiConversationService.createConversation(dto);
        return ResponseEntity.ok(conversation);
    }

    // API to get a list of conversations for a specific user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AiConversation>> getUserConversations(@PathVariable Integer userId) {
        List<AiConversation> conversations = aiConversationService.getUserConversations(userId);
        return ResponseEntity.ok(conversations);
    }

    // API to get a conversation by its ID
    @GetMapping("/{id}")
    public ResponseEntity<AiConversation> getConversationById(@PathVariable Long id) {
        AiConversation conversation = aiConversationService.getById(id);
        return ResponseEntity.ok(conversation);
    }
}
