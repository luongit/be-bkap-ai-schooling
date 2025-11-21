package com.bkap.aispark.repository.video_library_history;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.video_library_history.UserVideoHistory;

public interface UserVideoHistoryRepository extends JpaRepository<UserVideoHistory, Long> {
    List<UserVideoHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    void deleteByUserIdAndId(Long userId, Long id);
    boolean existsByUserIdAndId(Long userId, Long id);
}