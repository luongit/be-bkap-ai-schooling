package com.bkap.aispark.entity.voice_ai;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "voice_turns")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceTurn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;

    private String sceneCode;

    private String difficulty;

    private Integer turnIndex;

    @Column(columnDefinition = "text")
    private String userText;

    @Column(name = "ai_reply", columnDefinition = "text")
    private String aiReply;

    private String audioUrl;

    private Double pronunciationScore;
    private Double fluencyScore;
    private Double intonationScore;
    private Double confidenceScore;

    private String phonemeKey;

    private Instant createdAt;
}
