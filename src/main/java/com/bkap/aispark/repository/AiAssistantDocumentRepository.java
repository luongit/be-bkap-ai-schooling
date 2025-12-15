package com.bkap.aispark.repository;

import com.bkap.aispark.entity.AiAssistantDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AiAssistantDocumentRepository extends JpaRepository<AiAssistantDocument, Long> {
    List<AiAssistantDocument> findByAssistantId(Integer assistantId);
}
