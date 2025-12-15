package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;
import java.sql.Timestamp;

@Entity
@Table(name = "ai_assistant_documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AiAssistantDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "assistant_id", nullable = false)
    private Integer assistantId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_type")
    private String fileType;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "raw_text", columnDefinition = "text")
    private String rawText;

    @Column(name = "summary_text", columnDefinition = "text")
    private String summaryText;

    @Column(name = "created_at", updatable = false,
            columnDefinition = "timestamp default current_timestamp")
    private Timestamp createdAt;
}
