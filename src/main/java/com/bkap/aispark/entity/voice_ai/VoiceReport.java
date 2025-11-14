package com.bkap.aispark.entity.voice_ai;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "voice_reports")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoiceReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mindgraph_id")
    private UUID mindgraphId;

    @Column(name = "student_id")
    private Long studentId;

    @Column(name = "week_start")
    private LocalDate weekStart;

    @Column(name = "week_end")
    private LocalDate weekEnd;

    @Column(name = "total_turns")
    private Integer totalTurns;

    @Column(name = "avg_pronunciation")
    private Double avgPronunciation;

    @Column(name = "avg_fluency")
    private Double avgFluency;

    @Column(name = "avg_intonation")
    private Double avgIntonation;

    @Column(name = "avg_confidence")
    private Double avgConfidence;

    @Column(columnDefinition = "TEXT")
    private String progressSummary;

    @Column(columnDefinition = "TEXT")
    private String recommendation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
