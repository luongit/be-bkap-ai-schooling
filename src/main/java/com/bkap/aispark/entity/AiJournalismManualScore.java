package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.JsonNode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_journalism_manual_scores")
public class AiJournalismManualScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔗 Liên kết đến bài nộp
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "entry_id", nullable = false)
    private AiJournalismEntry entry;

    // 🔗 Liên kết đến giáo viên chấm
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    /**
     * Lưu JSON tiêu chí chấm dưới dạng JsonNode để tránh lỗi PGobject
     */
    @Column(columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private JsonNode criteria;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public AiJournalismManualScore() {}

    public AiJournalismManualScore(
            Long id,
            AiJournalismEntry entry,
            User teacher,
            BigDecimal totalScore,
            String feedback,
            JsonNode criteria,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.entry = entry;
        this.teacher = teacher;
        this.totalScore = totalScore;
        this.feedback = feedback;
        this.criteria = criteria;
        this.createdAt = createdAt;
    }

    // 👉 Getter / Setter đầy đủ

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AiJournalismEntry getEntry() { return entry; }
    public void setEntry(AiJournalismEntry entry) { this.entry = entry; }

    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }

    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public JsonNode getCriteria() { return criteria; }
    public void setCriteria(JsonNode criteria) { this.criteria = criteria; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
