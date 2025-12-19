package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "storybook_exports")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorybookExport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storybook_id", nullable = false)
    private Long storybookId;

    @Column(name = "export_type", nullable = false)
    private String exportType;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
