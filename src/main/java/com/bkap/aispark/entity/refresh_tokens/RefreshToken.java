package com.bkap.aispark.entity.refresh_tokens;
import java.time.LocalDateTime;
import com.bkap.aispark.entity.User;
import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mỗi refresh token thuộc về 1 user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Token JWT dài (unique)
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    // Ngày hết hạn 30 ngày sau khi login
    @Column(nullable = false)
    private LocalDateTime expiredAt;

    // Ngày tạo token
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // Đánh dấu token đã bị revoke khi logout
    @Column(nullable = false)
    private Boolean revoked = false;

    // ===== GETTER / SETTER =====

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public Boolean getRevoked() { return revoked; }
    public void setRevoked(Boolean revoked) { this.revoked = revoked; }
}
