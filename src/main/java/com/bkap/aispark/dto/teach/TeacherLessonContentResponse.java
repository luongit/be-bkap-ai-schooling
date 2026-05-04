package com.bkap.aispark.dto.teach;

import lombok.Data;
import java.util.List;

import com.bkap.aispark.entity.teach.enums.LessonStatus;

@Data
public class TeacherLessonContentResponse {
    private Long id;
    private String code;
    private String name;
    private Integer grade;
    private Integer teachingMonth;
    private String description;
    private String coverImage;
    private Integer lessonOrder;
    private LessonStatus lessonStatus;
    // Danh sách file đính kèm
    private List<LessonFileResponse> files;

}