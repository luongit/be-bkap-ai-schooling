package com.bkap.aispark.service;

import com.bkap.aispark.dto.ConversationCreateRequest;
import com.bkap.aispark.entity.AiAssistant;
import com.bkap.aispark.entity.AiConversation;
import com.bkap.aispark.repository.AiAssistantRepository;
import com.bkap.aispark.repository.AiConversationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiConversationService {

    private final AiConversationRepository conversationRepo;
    private final AiAssistantRepository assistantRepo;

    public AiConversationService(AiConversationRepository conversationRepo,
                                 AiAssistantRepository assistantRepo) {
        this.conversationRepo = conversationRepo;
        this.assistantRepo = assistantRepo;
    }
//
//    public AiConversation createConversation(ConversationCreateRequest dto) {
//        AiAssistant assistant = assistantRepo.findById(dto.getAssistantId())
//                .orElseThrow(() -> new RuntimeException("Assistant not found"));
//
//        String title = dto.getTitle();
//        if (title == null || title.isBlank()) {
//            title = "Cuộc trò chuyện mới với " + assistant.getName();
//        }
//
//        AiConversation conv = AiConversation.builder()
//                .assistant(assistant)
//                .userId(dto.getUserId())
//                .title(title)
//                .build();
//        
//        assistant.setUsed(assistant.getUsed() + 1);
//        assistantRepo.save(assistant);
//        return conversationRepo.save(conv);
//    }
    
    public AiConversation createConversation(ConversationCreateRequest dto) {
        AiAssistant assistant = assistantRepo.findById(dto.getAssistantId())
                .orElseThrow(() -> new RuntimeException("Assistant not found"));

        // === THÊM TỪ ĐÂY ===
        // Tăng lượt used mỗi khi có người tạo cuộc trò chuyện mới
        assistant.setUsed(assistant.getUsed() + 1);
        assistantRepo.save(assistant);
        // === ĐẾN ĐÂY ===

        String title = dto.getTitle();
        if (title == null || title.isBlank()) {
            title = "Cuộc trò chuyện mới với " + assistant.getName();
        }

        AiConversation conv = AiConversation.builder()
                .assistant(assistant)
                .userId(dto.getUserId())
                .title(title)
                .build();

        return conversationRepo.save(conv);
    }

    public List<AiConversation> getUserConversations(Integer userId) {
        return conversationRepo.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    public AiConversation getById(Long id) {
        return conversationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }
}
