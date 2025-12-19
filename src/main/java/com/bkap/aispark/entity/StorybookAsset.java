package com.bkap.aispark.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "storybook_assets")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorybookAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "storybook_id", nullable = false)
    private Long storybookId;

    @Column(name = "page_id")
    private Long pageId;

    @Column(name = "asset_type", nullable = false)
    private String assetType;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    private Long fileSize;
    private String mimeType;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
