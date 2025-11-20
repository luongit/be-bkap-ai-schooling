package com.bkap.aispark.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_image_library_capacity")
public class UserImageLibraryCapacity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    private int capacity;

    private int used;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public UserImageLibraryCapacity() {}

    public UserImageLibraryCapacity(Long userId, int capacity, int used) {
        this.userId = userId;
        this.capacity = capacity;
        this.used = used;
        this.updatedAt = LocalDateTime.now();
    }

    // ---------------------- Getter & Setter ---------------------
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getUsed() {
        return used;
    }

    public void setUsed(int used) {
        this.used = used;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
