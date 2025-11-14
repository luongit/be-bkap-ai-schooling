package com.bkap.aispark.entity.voice_ai;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "feedback_tips")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FeedbackTip {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String tag;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String category; // pronunciation / fluency / intonation / confidence
}
