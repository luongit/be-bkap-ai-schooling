package com.bkap.aispark.entity.Storybook;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "storybooks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Storybook {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    private String title;
    private String description;

    @Column(name = "original_prompt", nullable = false, columnDefinition = "TEXT")
    private String originalPrompt;

    private String language;

    @Column(name = "target_age")
    private String targetAge;

    @Column(name = "total_pages")
    private Integer totalPages;

    @Enumerated(EnumType.STRING)
    private StorybookStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== PROGRESS DETAIL =====
    @Enumerated(EnumType.STRING)
    private StorybookPhase progressPhase;

    private Integer progressCurrentPage;
    private Integer progressTotalPages;

    @Column(columnDefinition = "text")
    private String progressMessage;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        if (status == null)
            status = StorybookStatus.DRAFT;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
