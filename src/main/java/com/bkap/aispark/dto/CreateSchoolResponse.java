package com.bkap.aispark.dto;

public class CreateSchoolResponse {
    private Long schoolId;
    private String schoolName;
    private String adminEmail;
    private String tempPassword; // mật khẩu tạm (FE sẽ hiển thị 1 lần)
    private Long emailQueueId; // id của bản ghi queue email (có thể null nếu queue lỗi)
    private String emailStatus; // PENDING / SENT / FAILED / NOT_QUEUED

    public CreateSchoolResponse() {
    }

    public CreateSchoolResponse(Long schoolId, String schoolName, String adminEmail,
            String tempPassword, Long emailQueueId, String emailStatus) {
        this.schoolId = schoolId;
        this.schoolName = schoolName;
        this.adminEmail = adminEmail;
        this.tempPassword = tempPassword;
        this.emailQueueId = emailQueueId;
        this.emailStatus = emailStatus;
    }

    // getters & setters
    public Long getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(Long schoolId) {
        this.schoolId = schoolId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getAdminEmail() {
        return adminEmail;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public String getTempPassword() {
        return tempPassword;
    }

    public void setTempPassword(String tempPassword) {
        this.tempPassword = tempPassword;
    }

    public Long getEmailQueueId() {
        return emailQueueId;
    }

    public void setEmailQueueId(Long emailQueueId) {
        this.emailQueueId = emailQueueId;
    }

    public String getEmailStatus() {
        return emailStatus;
    }

    public void setEmailStatus(String emailStatus) {
        this.emailStatus = emailStatus;
    }
}
