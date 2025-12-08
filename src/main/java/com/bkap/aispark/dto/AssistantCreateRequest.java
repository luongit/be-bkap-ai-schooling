package com.bkap.aispark.dto;

import lombok.Data;

@Data
public class AssistantCreateRequest {
    private String name;
    private String description;
    private String systemPrompt;
    private Integer categoryId;
    private Boolean isPublished;
    private Integer authorId;
}
