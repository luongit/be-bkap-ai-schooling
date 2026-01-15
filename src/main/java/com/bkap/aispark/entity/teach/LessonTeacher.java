package com.bkap.aispark.entity.teach;

import com.bkap.aispark.entity.Teacher; // Import từ package cha
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "lesson_teachers",
    uniqueConstraints = @UniqueConstraint(columnNames = {"lesson_id", "teacher_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonTeacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher; 
    
    @Column(name = "assigned_at", updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    public void prePersist() {
        this.assignedAt = LocalDateTime.now();
    }
}