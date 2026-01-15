package com.bkap.aispark.entity.teach;

import com.bkap.aispark.entity.teach.enums.LessonFileType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "lesson_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LessonFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 30)
    private LessonFileType fileType;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "file_path", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    @Column(name = "folder_path", columnDefinition = "TEXT")
    private String folderPath;

    private Long fileSize;

    @Column(length = 64)
    private String checksum;

    @Column(nullable = false)
    private Integer version = 1;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "is_root", nullable = false)
    private Boolean isRoot = false;

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}