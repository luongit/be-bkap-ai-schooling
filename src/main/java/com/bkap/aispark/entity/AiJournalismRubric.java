package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_journalism_rubrics")
public class AiJournalismRubric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "contest_id", nullable = false)
    private AiJournalismContest contest;

    private String criterion; // Ví dụ: “Nội dung”
    private String description;
    private Double weight = 0.25;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== Constructors =====
    public AiJournalismRubric() {}

    
    public AiJournalismRubric(Long id, AiJournalismContest contest, String criterion, String description, Double weight,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.contest = contest;
		this.criterion = criterion;
		this.description = description;
		this.weight = weight;
		this.createdAt = createdAt;
	}


	// ===== Getters & Setters =====
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public AiJournalismContest getContest() {
        return contest;
    }
    public void setContest(AiJournalismContest contest) {
        this.contest = contest;
    }

    public String getCriterion() {
        return criterion;
    }
    public void setCriterion(String criterion) {
        this.criterion = criterion;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
