package com.bkap.aispark.entity.AiLearningOs.Document_ingestion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;  // Đảm bảo đã import JsonType

import java.sql.Timestamp;

@Entity
@Table(name = "ai_material_extractions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiMaterialExtraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "chunk_index", nullable = false)
    private int chunkIndex;

    @Column(name = "raw_text", columnDefinition = "text")
    private String rawText;

    @Column(name = "cleaned_text", columnDefinition = "text")
    private String cleanedText;

    @Column(name = "embeddings")
    private String embeddings;  // Đây là vector được lưu dưới dạng chuỗi (dùng PGVector hoặc xử lý ngoài)

    @Column(name = "detected_type", length = 50)
    private String detectedType;

    @Type(JsonType.class)  // Sử dụng JsonType để ánh xạ với jsonb
    @Column(name = "ai_notes", columnDefinition = "jsonb")
    private String aiNotes;

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Timestamp createdAt;
}
