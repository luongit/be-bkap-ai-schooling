package com.bkap.aispark.dto;

import java.time.LocalDateTime;

public class TeacherDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String code;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private String homeroomClassName;
    private String schoolName;

    public TeacherDTO() {
    }

    public TeacherDTO(Long id, String fullName, String email, String phone, String code,
            Boolean isActive, LocalDateTime createdAt,
            String homeroomClassName, String schoolName) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.code = code;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.homeroomClassName = homeroomClassName;
        this.schoolName = schoolName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getHomeroomClassName() {
        return homeroomClassName;
    }

    public void setHomeroomClassName(String homeroomClassName) {
        this.homeroomClassName = homeroomClassName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    // getter và setter
    // ...
}
