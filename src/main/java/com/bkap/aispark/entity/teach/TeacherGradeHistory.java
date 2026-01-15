package com.bkap.aispark.entity.teach;

import com.bkap.aispark.entity.teach.enums.TeacherGradeAction;
import com.bkap.aispark.entity.User;    // Import User từ AI Spark
import com.bkap.aispark.entity.Teacher; // Import Teacher từ AI Spark
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "teacher_grade_histories")
public class TeacherGradeHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by", nullable = false)
    private User performedBy;

    @Column(nullable = false)
    private Integer grade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TeacherGradeAction action;

    @Column(name = "action_at", nullable = false)
    private LocalDateTime actionAt;

    @PrePersist
    public void prePersist() {
        this.actionAt = LocalDateTime.now();
    }
}