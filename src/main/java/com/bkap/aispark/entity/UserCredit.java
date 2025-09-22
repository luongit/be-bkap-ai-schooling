package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_credits")
public class UserCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    private Integer credit = 100; // mặc định cấp 100

    private LocalDate expiredDate; // có thể null nếu chưa cần hạn dùng

    public UserCredit() {}

    public UserCredit(User user, Integer credit, LocalDate expiredDate) {
        this.user = user;
        this.credit = credit;
        this.expiredDate = expiredDate;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Integer getCredit() { return credit; }
    public void setCredit(Integer credit) { this.credit = credit; }

    public LocalDate getExpiredDate() { return expiredDate; }
    public void setExpiredDate(LocalDate expiredDate) { this.expiredDate = expiredDate; }
}
