package com.bkap.aispark.entity.voice_ai;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mindgraph_profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MindGraphProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mindgraph_id", unique = true, nullable = false)
    private UUID mindgraphId = UUID.randomUUID();

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "jsonb")
    private String data;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
