package com.bkap.aispark.service;

import com.bkap.aispark.dto.ConversationLogDTO;
import com.bkap.aispark.dto.SessionDTO;
import com.bkap.aispark.entity.ConversationLog;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.ConversationLogRepository;
import com.bkap.aispark.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ConversationLogService {

    @Autowired
    private ConversationLogRepository conversationLogRepository;

    @Autowired
    private UserRepository userRepository;

    // Lưu log mới (tạo sessionId mới nếu chưa có)
    public ConversationLog saveLog(Long userId, String message, String response, boolean violation, UUID sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id=" + userId));

        ConversationLog log = new ConversationLog();
        log.setUser(user);
        log.setMessage(message);
        log.setResponse(response);
        log.setViolationFlag(violation);
        log.setSessionId(sessionId != null ? sessionId : UUID.randomUUID());

        return conversationLogRepository.save(log);
    }

    // Lấy lịch sử theo user + session
    public List<ConversationLogDTO> getLogsBySessionDTO(Long userId, UUID sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id=" + userId));

        List<ConversationLog> logs = conversationLogRepository.findByUserAndSessionIdOrderByCreatedAt(user, sessionId);

        return logs.stream()
                .map(log -> new ConversationLogDTO(
                        log.getSessionId(),
                        log.getMessage(),
                        log.getResponse(),
                        log.getViolationFlag() != null ? log.getViolationFlag() : false,
                        log.getCreatedAt()
                ))
                .toList();
    }
    
    public List<SessionDTO> getSessionList(Long userId) {
        List<Object[]> results = conversationLogRepository.findDistinctSessionsByUser(userId);
        List<SessionDTO> sessions = new ArrayList<>();

        for (Object[] row : results) {
            UUID sessionId = (UUID) row[0];
            LocalDateTime createdAt = (LocalDateTime) row[1];

            // ✅ Lấy message đầu tiên của session
            ConversationLog firstLog = conversationLogRepository
                    .findTopByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId)
                    .orElse(null);

            String previewMessage = firstLog != null ? firstLog.getMessage() : "(Trống)";
            Instant createdAtInstant = createdAt.atZone(ZoneId.systemDefault()).toInstant();

            sessions.add(new SessionDTO(sessionId, createdAtInstant, previewMessage));
        }

        return sessions;
    }

    
 // Tạo session mới (tùy chọn: có thể lưu log "session started")
    public void createEmptySession(Long userId, UUID sessionId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ConversationLog initLog = new ConversationLog();
        initLog.setUser(user);
        initLog.setSessionId(sessionId);
        initLog.setCreatedAt(LocalDateTime.now(ZoneId.systemDefault()));
        initLog.setMessage("[Session started]"); // dùng message để preview
        initLog.setResponse(null);
        initLog.setViolationFlag(false);

        conversationLogRepository.save(initLog);
    }





}
