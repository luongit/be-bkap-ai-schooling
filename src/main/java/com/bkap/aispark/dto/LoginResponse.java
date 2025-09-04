package com.bkap.aispark.dto;

public class LoginResponse {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private String role;
    private String objectType;
    private Long objectId;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long userId, String username, String email, String phone,
                         String role, String objectType, Long objectId, String token) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.objectType = objectType;
        this.objectId = objectId;
        this.token = token;
    }

    // getters & setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getObjectType() { return objectType; }
    public void setObjectType(String objectType) { this.objectType = objectType; }

    public Long getObjectId() { return objectId; }
    public void setObjectId(Long objectId) { this.objectId = objectId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
