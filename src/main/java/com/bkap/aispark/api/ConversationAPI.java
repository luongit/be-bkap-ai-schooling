package com.bkap.aispark.api;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.bkap.aispark.dto.ConversationLogDTO;
import com.bkap.aispark.dto.SessionDTO;
import com.bkap.aispark.entity.ConversationLog;
import com.bkap.aispark.security.JwtUtil;
import com.bkap.aispark.service.ConversationLogService;

@RestController
@RequestMapping("/api/conversations")
public class ConversationAPI {

    @Autowired
    private ConversationLogService conversationLogService;

    @Autowired
    private JwtUtil jwtUtil;

    // 🔹 Lấy toàn bộ lịch sử của một session
    @GetMapping("/{sessionId}")
    public List<ConversationLogDTO> getConversationBySession(
            @PathVariable UUID sessionId,
            @RequestHeader("Authorization") String authHeader) {

        Long userId = extractUserId(authHeader);
        return conversationLogService.getLogsBySessionDTO(userId, sessionId);
    }

    // 🔹 Tạo session mới
    @PostMapping("/start")
    public Map<String, Object> startSession(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);

        UUID sessionId = UUID.randomUUID();
        conversationLogService.createEmptySession(userId, sessionId);

        return Map.of("sessionId", sessionId);
    }

    // 🔹 Lấy danh sách các session của user
   
    // Helper tách userId từ token
    private Long extractUserId(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserId(token);
        }
        throw new RuntimeException("Unauthorized");
    }
   
    @GetMapping("/sessions")
    public List<SessionDTO> getSessions(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return conversationLogService.getSessionList(userId);
    }

}
