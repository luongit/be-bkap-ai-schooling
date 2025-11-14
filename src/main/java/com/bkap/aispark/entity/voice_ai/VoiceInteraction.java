package com.bkap.aispark.entity.voice_ai;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "voice_interactions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VoiceInteraction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mindgraph_id")
    private UUID mindgraphId;

    @Column(name = "scene_code")
    private String sceneCode;

    @Column(name = "turn_index")
    private Integer turnIndex;

    @Column(name = "learner_text", columnDefinition = "TEXT")
    private String learnerText;

    @Column(name = "ai_reply", columnDefinition = "TEXT")
    private String aiReply;

    @Column(name = "feedback_tag")
    private String feedbackTag;

    @Column(name = "scores", columnDefinition = "jsonb")
    private String scores; // JSONB {pronun:80, flu:76, into:78, conf:85}

    @Column(name = "ai_score_avg")
    private Double aiScoreAvg;

    private String sentiment;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "teacher_id")
    private Long teacherId;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
