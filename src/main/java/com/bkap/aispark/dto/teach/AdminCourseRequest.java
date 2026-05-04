package com.bkap.aispark.dto.teach;

import com.bkap.aispark.entity.teach.enums.CourseStatus;

public class AdminCourseRequest {

    private String name;
    private Integer grade;
    private Integer teachingMonth;
    private String description;
    private String coverImage;
    private String videoUrl;
    private CourseStatus courseStatus;
    private Integer sortOrder;

    public AdminCourseRequest() {
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
}