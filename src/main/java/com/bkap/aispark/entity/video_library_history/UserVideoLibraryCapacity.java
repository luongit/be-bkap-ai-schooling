package com.bkap.aispark.entity.video_library_history;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_video_library_capacity")
public class UserVideoLibraryCapacity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private int capacity = 5;

    private int used = 0;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserVideoLibraryCapacity() {}

    public UserVideoLibraryCapacity(Long userId) {
        this.userId = userId;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters & Setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getUsed() { return used; }
    public void setUsed(int used) { this.used = used; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}