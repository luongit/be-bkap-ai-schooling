package com.bkap.aispark.dto;

public class LoginRequest {
    private String identifier; // login su dung email hoặc username
    private String password;
    private boolean rememberMe; // nho mat khau

    public LoginRequest() {}

    public LoginRequest(String identifier, String password, boolean rememberMe) {
        this.identifier = identifier;
        this.password = password;
        this.rememberMe = rememberMe;
    }

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRememberMe() {
        return rememberMe;
    }
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}
