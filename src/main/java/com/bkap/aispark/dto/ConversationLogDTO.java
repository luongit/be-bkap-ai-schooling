package com.bkap.aispark.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ConversationLogDTO {
    private UUID sessionId;
    private String message;
    private String response;
    private boolean violationFlag;
    private LocalDateTime createdAt;

    // constructor
    public ConversationLogDTO(UUID sessionId, String message, String response, boolean violationFlag, LocalDateTime createdAt) {
        this.sessionId = sessionId;
        this.message = message;
        this.response = response;
        this.violationFlag = violationFlag;
        this.createdAt = createdAt;
    }

    // getters/setters
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public boolean isViolationFlag() { return violationFlag; }
    public void setViolationFlag(boolean violationFlag) { this.violationFlag = violationFlag; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
