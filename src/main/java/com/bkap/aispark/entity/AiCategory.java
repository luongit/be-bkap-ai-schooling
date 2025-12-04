package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false)
    private String name;  

    @Column(nullable = false)
    private String label;  // tên hiển thị

    private LocalDateTime createdAt = LocalDateTime.now();
}
