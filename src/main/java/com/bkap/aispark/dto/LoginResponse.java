package com.bkap.aispark.dto;

public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String objectType;
    private Long objectId;
    private String accessToken; // token cũ (access token)
    private String refreshToken; // mới thêm

    public LoginResponse(Long userId, String username, String email, String phone, String role,
            String objectType, Long objectId, String accessToken, String refreshToken) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.objectType = objectType;
        this.objectId = objectId;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    // ✳️ Giữ nguyên constructor cũ (nếu phần khác của project đang dùng)
    public LoginResponse(Long id, String username, String email, String phone, String role,
            String objectType, Long objectId, String token) {
        this(id, username, email, phone, role, objectType, objectId, token, null);
    }

    // Getter & Setter (có thể dùng Lombok @Data)
    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRole() {
        return role;
    }

    public String getObjectType() {
        return objectType;
    }

    public Long getObjectId() {
        return objectId;
    }

    public String getAccessToken() {
        return accessToken;
    }


    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
