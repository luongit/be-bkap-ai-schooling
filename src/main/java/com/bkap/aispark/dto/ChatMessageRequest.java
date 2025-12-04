package com.bkap.aispark.dto;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long conversationId;
    private Integer userId;
    private String message;
}
