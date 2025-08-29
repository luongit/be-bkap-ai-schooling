package com.bkap.aispark.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String fullName;

	private String email;

	private String phone;

	@Column(unique = true, nullable = false)
	private String code; // Mã sinh viên/học sinh

	private Boolean isActive = true;
    
	@Column(name = "class_id", nullable = false)
	private Long classId;

	
	@Column(updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();

	public Student() {
		super();
		// TODO Auto-generated constructor stub
	}

	
	public Student(Long id, String fullName, String email, String phone, String code, Boolean isActive, Long classId,
			LocalDateTime createdAt) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.phone = phone;
		this.code = code;
		this.isActive = isActive;
		this.classId = classId;
		this.createdAt = createdAt;
	}


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}


	public  Long getClassId() {
		return classId;
	}


	public  void setClassId(Long classId) {
		this.classId = classId;
	}
	

}
