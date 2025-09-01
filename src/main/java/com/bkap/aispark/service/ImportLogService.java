package com.bkap.aispark.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bkap.aispark.entity.ImportLog;
import com.bkap.aispark.repository.ImportLogRepository;

@Service
public class ImportLogService {

    @Autowired
    private ImportLogRepository importLogRepository;

    public List<ImportLog> getAllLogs() {
        return importLogRepository.findAll();
    }

    public Optional<ImportLog> getLogById(Long id) {
        return importLogRepository.findById(id);
    }

    public ImportLog saveLog(ImportLog log) {
        return importLogRepository.save(log);
    }

    public boolean deleteLog(Long id) {
        return importLogRepository.findById(id).map(log -> {
            importLogRepository.delete(log);
            return true;
        }).orElse(false);
    }
}
