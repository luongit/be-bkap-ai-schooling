package com.bkap.aispark.entity.teach;

import com.bkap.aispark.entity.teach.enums.CourseStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer grade;

    @Column(name = "teaching_month", nullable = false)
    private Integer teachingMonth;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_image")
    private String coverImage;

    @Column(name = "video_url", columnDefinition = "TEXT")
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_status", nullable = false, length = 20)
    private CourseStatus courseStatus = CourseStatus.ACTIVE;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public Course() {
    }

    public Course(
            Long id,
            String name,
            Integer grade,
            Integer teachingMonth,
            String description,
            String coverImage,
            String videoUrl,
            CourseStatus courseStatus,
            Integer sortOrder,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.grade = grade;
        this.teachingMonth = teachingMonth;
        this.description = description;
        this.coverImage = coverImage;
        this.videoUrl = videoUrl;
        this.courseStatus = courseStatus;
        this.sortOrder = sortOrder;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;

        if (this.courseStatus == null) {
            this.courseStatus = CourseStatus.ACTIVE;
        }

        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();

        if (this.courseStatus == null) {
            this.courseStatus = CourseStatus.ACTIVE;
        }

        if (this.sortOrder == null) {
            this.sortOrder = 0;
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getGrade() {
        return grade;
    }

    public Integer getTeachingMonth() {
        return teachingMonth;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public CourseStatus getCourseStatus() {
        return courseStatus;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public void setTeachingMonth(Integer teachingMonth) {
        this.teachingMonth = teachingMonth;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCourseStatus(CourseStatus courseStatus) {
        this.courseStatus = courseStatus;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}