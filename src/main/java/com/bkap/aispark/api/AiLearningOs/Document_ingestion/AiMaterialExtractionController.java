package com.bkap.aispark.api.AiLearningOs.Document_ingestion;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bkap.aispark.entity.AiLearningOs.Document_ingestion.AiMaterialExtraction;
import com.bkap.aispark.service.AiLearningOs.Document_ingestion.AiMaterialExtractionService;

@RestController
@RequestMapping("/materials/extractions")
public class AiMaterialExtractionController {

    @Autowired
    private AiMaterialExtractionService extractionService;

    // API để chia tài liệu thành các đoạn nhỏ
    @PostMapping("/extract-chunks")
    public ResponseEntity<?> extractChunks(@RequestBody AiMaterialExtraction extraction) {
        try {
            List<AiMaterialExtraction> chunks = extractionService.extractChunks(extraction);
            return ResponseEntity.ok(chunks); // Trả về danh sách các chunk đã được lưu
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API để tạo extraction mới
    @PostMapping
    public ResponseEntity<?> createExtraction(@RequestBody AiMaterialExtraction extraction) {
        try {
            AiMaterialExtraction savedExtraction = extractionService.saveExtraction(extraction);
            return ResponseEntity.ok(savedExtraction); // Trả về extraction đã được lưu
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // API để lấy extraction theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getExtraction(@PathVariable Long id) {
        return extractionService.getExtractionById(id)
                .map(ResponseEntity::ok) // Trả về extraction nếu tìm thấy
                .orElseGet(() -> ResponseEntity.notFound().build()); // Trả về 404 nếu không tìm thấy
    }
}
