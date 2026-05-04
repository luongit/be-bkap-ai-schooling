package com.bkap.aispark.dto.teach;

import java.util.List;

public class AdminTeacherGradeRequest {

    private List<Integer> grades;

    public AdminTeacherGradeRequest() {
    }

    public List<Integer> getGrades() {
        return grades;
    }

    public void setGrades(List<Integer> grades) {
        this.grades = grades;
    }
}