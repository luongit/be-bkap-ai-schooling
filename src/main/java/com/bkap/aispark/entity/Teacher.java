package com.bkap.aispark.entity;


import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "teachers")
public class Teacher {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", nullable = false)
	private String fullName;

	private String email;

	private String phone;

	@Column(unique = true, nullable = false)
	private String code;

	private Boolean isActive = true;
    

	@Column(updatable = false)
	private LocalDateTime createdAt = LocalDateTime.now();
	
	
	@OneToOne
	@JoinColumn(name = "homeroom_class_id", referencedColumnName = "id", unique = true)
	private Classes homeroomClass;


	public Teacher() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Teacher(Long id, String fullName, String email, String phone, String code, Boolean isActive,
			LocalDateTime createdAt, Classes homeroomClass) {
		super();
		this.id = id;
		this.fullName = fullName;
		this.email = email;
		this.phone = phone;
		this.code = code;
		this.isActive = isActive;
		this.createdAt = createdAt;
		this.homeroomClass = homeroomClass;
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

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}



	public  Classes getHomeroomClass() {
		return homeroomClass;
	}



	public  void setHomeroomClass(Classes homeroomClass) {
		this.homeroomClass = homeroomClass;
	}
     
	
}


