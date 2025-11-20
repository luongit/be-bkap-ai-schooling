
package com.bkap.aispark.repository;

import com.bkap.aispark.entity.UserImageHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserImageHistoryRepository extends JpaRepository<UserImageHistory, Long> {
    
    /**
     * Lấy danh sách lịch sử theo userId, sắp xếp theo thời gian mới nhất
     */
    List<UserImageHistory> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Lấy danh sách theo userId và status
     */
    List<UserImageHistory> findByUserIdAndStatus(Long userId, String status);
    
    /**
     * Đếm số lượng ảnh theo userId và status
     */
    long countByUserIdAndStatus(Long userId, String status);
    
    /**
     * Đếm tổng số ảnh của user
     */
    long countByUserId(Long userId);
    
    List<UserImageHistory> findByUserIdOrderByCreatedAtDesc(Long userId);
    
}