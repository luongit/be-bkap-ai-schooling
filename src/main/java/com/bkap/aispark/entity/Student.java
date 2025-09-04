package com.bkap.aispark.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(name = "default_password", nullable = false)
    private String defaultPassword;

    @Column(unique = true, nullable = false)
    private String code; // Mã học sinh nội bộ, backend tự sinh

    // 👉 Liên kết tới bảng classes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Classes classEntity;

    @Column(nullable = true)
    private String email;

    private String phone;

    @Column(name = "hobbies")
    private String hobbies;

    private Boolean isActive = true;

    private LocalDate birthdate;

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Student() {}

    public Student(Long id, String fullName, String username, String defaultPassword, String code,
                   Classes classEntity, String email, String phone, String hobbies,
                   Boolean isActive, LocalDate birthdate, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.defaultPassword = defaultPassword;
        this.code = code;
        this.classEntity = classEntity;
        this.email = email;
        this.phone = phone;
        this.hobbies = hobbies;
        this.isActive = isActive;
        this.birthdate = birthdate;
        this.createdAt = createdAt;
    }

    // Getters & Setters

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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDefaultPassword() {
        return defaultPassword;
    }

    public void setDefaultPassword(String defaultPassword) {
        this.defaultPassword = defaultPassword;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Classes getClassEntity() {
        return classEntity;
    }

    public void setClassEntity(Classes classEntity) {
        this.classEntity = classEntity;
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

    public String getHobbies() {
        return hobbies;
    }

    public void setHobbies(String hobbies) {
        this.hobbies = hobbies;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
