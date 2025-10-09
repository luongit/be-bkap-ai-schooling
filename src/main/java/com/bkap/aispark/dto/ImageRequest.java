package com.bkap.aispark.dto;

public class ImageRequest {

    private Long userId;
    private String prompt;
    private String style;
    private String size;

    public ImageRequest() {
    }

    public ImageRequest(Long userId, String prompt, String style, String size) {
        this.userId = userId;
        this.prompt = prompt;
        this.style = style;
        this.size = size;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }
}
