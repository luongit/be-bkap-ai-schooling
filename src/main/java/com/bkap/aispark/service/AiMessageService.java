package com.bkap.aispark.service;

import com.bkap.aispark.entity.*;
import com.bkap.aispark.repository.*;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiMessageService {

    private final AiMessageRepository messageRepo;
    private final AiConversationRepository conversationRepo;

    public AiMessage saveMessage(Long conversationId, String role, String content) {
        AiConversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        AiMessage msg = AiMessage.builder()
                .conversation(conv)
                .role(role)
                .content(content)
                .build();

        return messageRepo.save(msg);
    }
 // Trong AiMessageService.java
    public List<AiMessage> getMessagesByConversationId(Long conversationId) {
        return messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }
}
