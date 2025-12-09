package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "ai_conversations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiConversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Trợ lý AI
    @ManyToOne
    @JoinColumn(name = "assistant_id", nullable = false)
    private AiAssistant assistant;

    // Người dùng chat (trỏ tới bảng users)
    @Column(nullable = false)
    private Integer userId;

    private String title;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
