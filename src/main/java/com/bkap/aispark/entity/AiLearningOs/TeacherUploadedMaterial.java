package com.bkap.aispark.entity.AiLearningOs;

import java.sql.Timestamp;
import java.util.Map;

import org.hibernate.annotations.Type;
import com.vladmihalcea.hibernate.type.json.JsonType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "teacher_uploaded_materials")
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class TeacherUploadedMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "teacher_id", nullable = false)
    private Long teacherId;

    @Column(name = "material_type")
    private String materialType;

    private String title;
    private String description;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(columnDefinition = "text")
    private String rawText;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> meta;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> aiExtractedNodes;

    @Column(name = "ai_summary", columnDefinition = "text")
    private String aiSummary;

    @Column(name = "created_at")
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    // GETTER + SETTER
}
