package com.bkap.aispark.dto;

import com.bkap.aispark.entity.Classes;

public class ClassesDTO {
    private Long id;
    private String name;
    private Long schoolId;
    private String schoolName;
    private String schoolAddress;

    public ClassesDTO(Classes classes) {
        this.id = classes.getId();
        this.name = classes.getName();
        if (classes.getSchool() != null) {
            this.schoolId = classes.getSchool().getId();
            this.schoolName = classes.getSchool().getName();
            this.schoolAddress = classes.getSchool().getAddress();
        }
    }

    // getters & setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolAddress() {
        return schoolAddress;
    }

    public void setSchoolAddress(String schoolAddress) {
        this.schoolAddress = schoolAddress;
    }
}