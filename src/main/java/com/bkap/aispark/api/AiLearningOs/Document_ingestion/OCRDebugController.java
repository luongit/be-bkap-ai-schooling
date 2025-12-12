package com.bkap.aispark.api.AiLearningOs.Document_ingestion;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bkap.aispark.service.AiLearningOs.Document_ingestion.OCRService;

@RestController
@RequestMapping("/debug/ocr")
public class OCRDebugController {

    private final OCRService ocrService;

    public OCRDebugController(OCRService ocrService) {
        this.ocrService = ocrService;
    }

    @PostMapping
    public ResponseEntity<?> testOCR(@RequestParam("file") MultipartFile file) {
        try {
            String text = ocrService.extractText(file);
            return ResponseEntity.ok(text);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
