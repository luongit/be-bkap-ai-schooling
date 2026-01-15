package com.bkap.aispark.dto.teach;

import lombok.Data;

@Data
public class LessonFileResponse {
    private Long id;
    private String fileType;
    private String fileName;
    private String filePath;
    private String folderPath;
    private Long fileSize;
    private Boolean isRoot;
}