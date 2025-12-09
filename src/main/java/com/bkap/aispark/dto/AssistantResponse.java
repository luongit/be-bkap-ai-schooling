package com.bkap.aispark.dto;

import lombok.Data;

@Data
public class AssistantResponse {

    private Integer id;
    private String name;
    private String description;
    private String avatarUrl;

    private Long views;
    private Long used;

    private Integer authorId;
    private String authorFullName;
    private String authorAvatar;
}
