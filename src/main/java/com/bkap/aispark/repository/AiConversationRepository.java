package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiConversation;
import com.bkap.aispark.entity.AiAssistant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiConversationRepository extends JpaRepository<AiConversation, Long> {
    List<AiConversation> findAllByUserIdOrderByCreatedAtDesc(Integer userId);
    List<AiConversation> findAllByAssistant(AiAssistant assistant);
}
