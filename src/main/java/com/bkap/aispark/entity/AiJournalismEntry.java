package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_journalism_entries")
public class AiJournalismEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    private AiJournalismContest contest;

    @Column(name = "student_id")
    private Long studentId; // tham chiếu students.id

    private String title;

    @Column(columnDefinition = "TEXT")
    private String article;

    @Column(name = "ai_score")
    private Double aiScore;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    // 🔹 Thêm cột JSON để lưu điểm từng tiêu chí (vd: {"Nội dung":22, "Cảm xúc":21, ...})
    @Column(name = "ai_criteria", columnDefinition = "JSON")
    private String aiCriteria;

    @Column(name = "teacher_feedback", columnDefinition = "TEXT")
    private String teacherFeedback;

    private String status = "SUBMITTED";

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== Constructors =====
    public AiJournalismEntry() {}

    public AiJournalismEntry(Long id, AiJournalismContest contest, Long studentId, String title, String article,
                             Double aiScore, String aiFeedback, String aiCriteria,
                             String teacherFeedback, String status, LocalDateTime createdAt) {
        this.id = id;
        this.contest = contest;
        this.studentId = studentId;
        this.title = title;
        this.article = article;
        this.aiScore = aiScore;
        this.aiFeedback = aiFeedback;
        this.aiCriteria = aiCriteria;
        this.teacherFeedback = teacherFeedback;
        this.status = status;
        this.createdAt = createdAt;
    }

    // ===== Getters & Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AiJournalismContest getContest() { return contest; }
    public void setContest(AiJournalismContest contest) { this.contest = contest; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArticle() { return article; }
    public void setArticle(String article) { this.article = article; }

    public Double getAiScore() { return aiScore; }
    public void setAiScore(Double aiScore) { this.aiScore = aiScore; }

    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }

    public String getAiCriteria() { return aiCriteria; }
    public void setAiCriteria(String aiCriteria) { this.aiCriteria = aiCriteria; }

    public String getTeacherFeedback() { return teacherFeedback; }
    public void setTeacherFeedback(String teacherFeedback) { this.teacherFeedback = teacherFeedback; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
