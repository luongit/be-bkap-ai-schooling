package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "storybook_ai_configs")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorybookAiConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storybook_id", nullable = false)
    private Long storybookId;

    private String textProvider;
    private String textModel;

    private String imageProvider;
    private String imageModel;

    private String ttsProvider;
    private String ttsModel;
    private String ttsVoice;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
