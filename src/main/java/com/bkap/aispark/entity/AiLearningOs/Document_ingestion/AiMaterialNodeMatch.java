package com.bkap.aispark.entity.AiLearningOs.Document_ingestion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "ai_material_node_matches")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AiMaterialNodeMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "extraction_id", nullable = false)
    private Long extractionId;

    @Column(name = "node_id", nullable = false)
    private Long nodeId;

    @Column(name = "confidence_score", precision = 5, scale = 2)
private BigDecimal confidenceScore;


    @Column(name = "match_reason", columnDefinition = "jsonb")
    private String matchReason;

    @Column(name = "created_at", updatable = false, columnDefinition = "timestamp default current_timestamp")
    private Timestamp createdAt;

}
