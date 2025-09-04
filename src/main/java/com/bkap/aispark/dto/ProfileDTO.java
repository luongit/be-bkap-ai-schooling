package com.bkap.aispark.dto;

import com.bkap.aispark.entity.UserRole;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import com.bkap.aispark.entity.ObjectType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDTO {
    private Long userId;
    private String username;
    private String email;
    private String phone;
    private UserRole role;
    private ObjectType objectType;
    // Thông tin chi tiết
    private String fullName;
    private String code;
    private String className;   // nếu student
    private String homeroom;  // nếu teacher
    
    private List<String> hobbies;

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }

    public ObjectType getObjectType() {
        return objectType;
    }
    public void setObjectType(ObjectType objectType) {
        this.objectType = objectType;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCode() {
        return code;
    }
    public void setCode(String code) {
        this.code = code;
    }

    public String getClassName() {
        return className;
    }
    public void setClassName(String className) {
        this.className = className;
    }

    public String getHomeroom() {
        return homeroom;
    }
    public void setHomeroom(String homeroom) {
        this.homeroom = homeroom;
    }
	public List<String> getHobbies() {
		return hobbies;
	}
	public void setHobbies(List<String> hobbies) {
		this.hobbies = hobbies;
	}
	
    
} 
