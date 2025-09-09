package com.bkap.aispark.repository;

import com.bkap.aispark.entity.ConversationLog;
import com.bkap.aispark.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ConversationLogRepository extends JpaRepository<ConversationLog, Long> {

    // Lấy tất cả log của user (mới nhất trước)
    List<ConversationLog> findByUserOrderByCreatedAtDesc(User user);

    // Lấy toàn bộ log của 1 session (theo thứ tự thời gian)
    List<ConversationLog> findByUserAndSessionIdOrderByCreatedAt(User user, UUID sessionId);

    // Lấy danh sách các session (mỗi sessionId + thời điểm bắt đầu)
    @Query("SELECT c.sessionId as sessionId, MIN(c.createdAt) as createdAt " +
           "FROM ConversationLog c " +
           "WHERE c.user.id = :userId " +
           "GROUP BY c.sessionId " +
           "ORDER BY MIN(c.createdAt) DESC")
    List<Object[]> findDistinctSessionsByUser(Long userId);

    // Lấy log đầu tiên của 1 session (sớm nhất)
    Optional<ConversationLog> findTopByUserIdAndSessionIdOrderByCreatedAtAsc(Long userId, UUID sessionId);

    // Lấy log cuối cùng KHÁC "[Session started]" (dùng cho preview)
    Optional<ConversationLog> findTopByUserIdAndSessionIdAndMessageNotOrderByCreatedAtDesc(
            Long userId, UUID sessionId, String message);
    Optional<ConversationLog> findTopByUserIdAndSessionIdAndMessageNotOrderByCreatedAtAsc(
            Long userId, UUID sessionId, String excludedMessage);
    
    void deleteByUserAndSessionId(User user, UUID sessionId);



}
