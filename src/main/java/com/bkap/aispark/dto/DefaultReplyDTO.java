package com.bkap.aispark.dto;

public class DefaultReplyDTO {
    private String replyText;
    private Long createdById;

    // Getters & Setters
    public String getReplyText() {
        return replyText;
    }
    public void setReplyText(String replyText) {
        this.replyText = replyText;
    }

    public Long getCreatedById() {
        return createdById;
    }
    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}
