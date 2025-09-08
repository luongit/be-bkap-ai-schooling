package com.bkap.aispark.dto;

public class TeacherRequestDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String code;
    private Boolean isActive;
    private Long classId; // chỉ cần gửi classId

    public TeacherRequestDTO() {}

    // getter & setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
}
