package com.bkap.aispark.dto;
public class ForbiddenKeywordDTO {
    private String keyword;
    private Long createdById;
    public String getKeyword() {
        return keyword;
    }
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public Long getCreatedById() {
        return createdById;
    }
    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }

    // getters/setters
}
