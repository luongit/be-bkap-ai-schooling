package com.bkap.aispark.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "parents")
public class Parent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String code;
    private String name;
    private String phone;
    private String email;
    private String address;

    // Bắt buộc: Constructor rỗng cho JPA
    public Parent() {
    }

    // Constructor có tham số (của bạn)
    public Parent(String address,String code , String email, Long id, String name, String phone) {
        this.address = address;
        this.email = email;
        this.id = id;
        this.code =code;
        this.name = name;
        this.phone = phone;
    }

    //  Getter & Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCode() {return code;}
    public void setCode(String code) {
        this.code = code;
    }
}
