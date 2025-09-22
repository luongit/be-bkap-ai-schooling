package com.bkap.aispark.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bkap.aispark.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByTargetTableOrderByCreatedAtDesc(String targetTable);

}
