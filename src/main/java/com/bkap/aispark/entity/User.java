package com.bkap.aispark.entity;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;


import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "user_role", nullable = false)
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(columnDefinition = "object_type_enum")
	@JdbcType(PostgreSQLEnumJdbcType.class)
	private ObjectType objectType;

	private Long objectId;

	@Column(unique = true)
	private String email;

	private String phone;

	private String studentCode;

	@Column(nullable = false)
	private Boolean isActive = true;

	@CreationTimestamp
	@Column(updatable = false)
	private LocalDateTime createdAt;

	// Constructors
	public User() {
	}

	public User(Long id, String password, UserRole role, ObjectType objectType, Long objectId, String email,
			String phone, String studentCode, Boolean isActive, LocalDateTime createdAt) {
		this.id = id;
		this.password = password;
		this.role = role;
		this.objectType = objectType;
		this.objectId = objectId;
		this.email = email;
		this.phone = phone;
		this.studentCode = studentCode;
		this.isActive = isActive;
		this.createdAt = createdAt;
	}

	// Getters & Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public Long getObjectId() {
		return objectId;
	}

	public void setObjectId(Long objectId) {
		this.objectId = objectId;
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

	public String getStudentCode() {
		return studentCode;
	}

	public void setStudentCode(String studentCode) {
		this.studentCode = studentCode;
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
}
