package com.bkap.aispark.entity.teach;

import com.bkap.aispark.entity.teach.enums.LessonStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer grade;

    @Column(name = "teaching_month", nullable = false)
    private Integer teachingMonth;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder = 0;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image")
    private String coverImage;

    @Enumerated(EnumType.STRING)
    @Column(name = "lesson_status", length = 20, nullable = false)
    private LessonStatus lessonStatus = LessonStatus.ACTIVE;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        if (this.version == null) {
            this.version = 1;
        }

        if (this.lessonOrder == null) {
            this.lessonOrder = 0;
        }

        if (this.lessonStatus == null) {
            this.lessonStatus = LessonStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.version == null) {
            this.version = 1;
        } else {
            this.version++;
        }
    }
}