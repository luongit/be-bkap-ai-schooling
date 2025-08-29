package com.bkap.aispark.dto;


public class LoginResponse {
    private Long userId;
    private String email;
    private String phone;
    private String role;
    private String objectType;
    private Long objectId;
    private String studentCode;
	public LoginResponse(Long userId, String email, String phone, String role, String objectType, Long objectId,
			String studentCode) {
		super();
		this.userId = userId;
		this.email = email;
		this.phone = phone;
		this.role = role;
		this.objectType = objectType;
		this.objectId = objectId;
		this.studentCode = studentCode;
	}
	public LoginResponse() {
		super();
		// TODO Auto-generated constructor stub
	}
	public final Long getUserId() {
		return userId;
	}
	public final void setUserId(Long userId) {
		this.userId = userId;
	}
	public final String getEmail() {
		return email;
	}
	public final void setEmail(String email) {
		this.email = email;
	}
	public  String getPhone() {
		return phone;
	}
	public  void setPhone(String phone) {
		this.phone = phone;
	}
	public  String getRole() {
		return role;
	}
	public  void setRole(String role) {
		this.role = role;
	}
	public  String getObjectType() {
		return objectType;
	}
	public  void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	public  Long getObjectId() {
		return objectId;
	}
	public  void setObjectId(Long objectId) {
		this.objectId = objectId;
	}
	public  String getStudentCode() {
		return studentCode;
	}
	public  void setStudentCode(String studentCode) {
		this.studentCode = studentCode;
	}

    
}

