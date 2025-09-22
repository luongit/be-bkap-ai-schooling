package com.bkap.aispark.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.AuditLog;
import com.bkap.aispark.repository.AuditLogRepository;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(Long userId, String action, String targetTable, Long targetId, Map<String, Object> details) {
        AuditLog log = new AuditLog(userId, action, targetTable, targetId, details);
        auditLogRepository.save(log);
    }
}