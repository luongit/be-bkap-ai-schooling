package com.bkap.aispark.api;

import com.bkap.aispark.entity.AuditLog;
import com.bkap.aispark.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogApi {

    @Autowired
    private AuditLogRepository auditLogRepository;

    // GET ALL
    @GetMapping
    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    @GetMapping("/by-table/{table}")
    public List<AuditLog> getLogsByTable(@PathVariable String table) {
        return auditLogRepository.findByTargetTableOrderByCreatedAtDesc(table);
    }
}
