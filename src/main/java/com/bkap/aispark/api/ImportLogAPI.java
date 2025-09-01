package com.bkap.aispark.api;

import com.bkap.aispark.entity.ImportLog;
import com.bkap.aispark.service.ImportLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/import-logs")
public class ImportLogAPI {

    @Autowired
    private ImportLogService importLogService;

    // Lấy toàn bộ log
    @GetMapping
    public List<ImportLog> getAllLogs() {
        return importLogService.getAllLogs();
    }

    // Lấy theo id
    @GetMapping("/{id}")
    public ResponseEntity<ImportLog> getLogById(@PathVariable Long id) {
        return importLogService.getLogById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Thêm log mới
    @PostMapping
    public ResponseEntity<ImportLog> createLog(@RequestBody ImportLog log) {
        ImportLog saved = importLogService.saveLog(log);
        return ResponseEntity.ok(saved);
    }

    // Cập nhật log
    @PutMapping("/{id}")
    public ResponseEntity<ImportLog> updateLog(@PathVariable Long id, @RequestBody ImportLog newLog) {
        return importLogService.getLogById(id)
                .map(existing -> {
                    existing.setFileName(newLog.getFileName());
                    existing.setImportedBy(newLog.getImportedBy());
                    existing.setTotalRecords(newLog.getTotalRecords());
                    existing.setSuccessCount(newLog.getSuccessCount());
                    existing.setErrorCount(newLog.getErrorCount());
                    existing.setErrors(newLog.getErrors());
                    ImportLog updated = importLogService.saveLog(existing);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Xóa log
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLog(@PathVariable Long id) {
        boolean deleted = importLogService.deleteLog(id);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }
}
