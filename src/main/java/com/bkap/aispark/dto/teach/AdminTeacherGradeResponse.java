package com.bkap.aispark.dto.teach;

import java.util.List;

public class AdminTeacherGradeResponse {

    private Long teacherId;
    private String teacherName;
    private String email;
    private String code;
    private String homeroomClassName;
    private List<Integer> assignedGrades;

    public AdminTeacherGradeResponse() {
    }

    public Long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(Long teacherId) {
        this.teacherId = teacherId;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


    public String getHomeroomClassName() {
        return homeroomClassName;
    }

    public void setHomeroomClassName(String homeroomClassName) {
        this.homeroomClassName = homeroomClassName;
    }

    public List<Integer> getAssignedGrades() {
        return assignedGrades;
    }

    public void setAssignedGrades(List<Integer> assignedGrades) {
        this.assignedGrades = assignedGrades;
    }
}