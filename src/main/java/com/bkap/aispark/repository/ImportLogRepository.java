package com.bkap.aispark.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bkap.aispark.entity.ImportLog;

public interface ImportLogRepository extends JpaRepository<ImportLog, Long> {
   
}
