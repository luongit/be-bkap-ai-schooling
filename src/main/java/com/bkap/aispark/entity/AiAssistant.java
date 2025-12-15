package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_assistants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiAssistant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String systemPrompt;

    private String model = "gpt-4o";

    @Column(unique = true)
    private String publicSlug;

    private String avatarUrl;

    // Mỗi trợ lý chỉ có 1 category
    @ManyToOne
    @JoinColumn(name = "category_id")
    private AiCategory category;

    // Trỏ tới bảng users
    @Column(name = "author_id")
    private Integer authorId;

    private Boolean isPublished = false;

    @Builder.Default
    @Column(nullable = false)
    private Long views = 0L;

    @Builder.Default
    @Column(nullable = false)
    private Long used = 0L;

    private LocalDateTime createdAt = LocalDateTime.now();
}
