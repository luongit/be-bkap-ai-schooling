package com.bkap.aispark.dto;

import lombok.Data;

@Data
public class ConversationCreateRequest {
    private Integer assistantId;
    private Integer userId;
    private String title;   // optional
}
