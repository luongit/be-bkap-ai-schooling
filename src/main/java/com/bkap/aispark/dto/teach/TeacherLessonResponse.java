package com.bkap.aispark.dto.teach;

import lombok.Data;

@Data
public class TeacherLessonResponse {
    private Long id;
    private String code;
    private String name;
    private Integer grade;
    private Integer teachingMonth;
    private String coverImage;
}