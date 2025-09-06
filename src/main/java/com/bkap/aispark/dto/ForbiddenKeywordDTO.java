package com.bkap.aispark.dto;

public class ForbiddenKeywordDTO {
    private Long id;
    private String keyword;
    private UserDTO createdBy;

    public ForbiddenKeywordDTO() {
    }

    public ForbiddenKeywordDTO(Long id, String keyword, UserDTO createdBy) {
        this.id = id;
        this.keyword = keyword;
        this.createdBy = createdBy;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public UserDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {
        this.createdBy = createdBy;
    }
}