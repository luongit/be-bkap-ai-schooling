package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiMessage;
import com.bkap.aispark.entity.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findAllByConversationOrderByCreatedAtAsc(AiConversation conversation);

    List<AiMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}
