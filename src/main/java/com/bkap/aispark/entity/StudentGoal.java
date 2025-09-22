package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_goals")
public class StudentGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Liên kết tới student
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Student student;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String goal;

    @Column(length = 20)
    private String status = "PENDING"; // PENDING | IN_PROGRESS | DONE

    private LocalDate deadline;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Constructors
    public StudentGoal() {}

    public StudentGoal(Long id, Student student, String goal, String status, LocalDate deadline, LocalDateTime createdAt) {
        this.id = id;
        this.student = student;
        this.goal = goal;
        this.status = status;
        this.deadline = deadline;
        this.createdAt = createdAt;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public String getGoal() {
        return goal;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
