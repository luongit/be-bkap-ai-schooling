package com.bkap.aispark.service;

import com.bkap.aispark.entity.ConversationLog;
import com.bkap.aispark.entity.User;
import com.bkap.aispark.repository.ConversationLogRepository;
import com.bkap.aispark.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConversationLogService {

    @Autowired
    private ConversationLogRepository conversationLogRepository;

    @Autowired
    private UserRepository userRepository;

    public ConversationLog saveLog(Long userId, String message, String response, boolean violation) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id=" + userId));

        ConversationLog log = new ConversationLog();
        log.setUser(user);
        log.setMessage(message);
        log.setResponse(response);
        log.setViolationFlag(violation);

        return conversationLogRepository.save(log);
    }

    public List<ConversationLog> getLogsByUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User không tồn tại với id=" + userId));
        return conversationLogRepository.findByUserOrderByCreatedAtDesc(user);
    }
}

