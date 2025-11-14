package com.bkap.aispark.entity.voice_ai;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "voice_scenes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoiceScene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code; // ví dụ: cafe-standard

    private String title;
    private String cefrLevel;
    private String difficulty;

    @Column(columnDefinition = "TEXT")
    private String goal;

    @Column(columnDefinition = "TEXT")
    private String requiredVocab;

    @Column(columnDefinition = "TEXT")
    private String patterns;

    @Column(columnDefinition = "jsonb")
    private String jsonData;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
