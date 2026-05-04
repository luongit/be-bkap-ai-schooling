package com.bkap.aispark.dto.teach;

import com.bkap.aispark.entity.teach.enums.LessonStatus;

public class AdminLessonRequest {

    private Long courseId;
    private String code;
    private String name;
    private Integer grade;
    private Integer teachingMonth;
    private Integer lessonOrder;
    private String description;
    private String coverImage;
    private LessonStatus lessonStatus;

    public AdminLessonRequest() {
    }

    public Long getCourseId() {
        return courseId;
    }

    public String getCode() {
        return code;
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

    public Integer getLessonOrder() {
        return lessonOrder;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public LessonStatus getLessonStatus() {
        return lessonStatus;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public void setCode(String code) {
        this.code = code;
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

    public void setLessonOrder(Integer lessonOrder) {
        this.lessonOrder = lessonOrder;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public void setLessonStatus(LessonStatus lessonStatus) {
        this.lessonStatus = lessonStatus;
    }
}