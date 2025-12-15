package com.bkap.aispark.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class TextExtractorService {

    private final com.bkap.aispark.service.AiLearningOs.Document_ingestion.OCRService ocrService;

    public TextExtractorService(com.bkap.aispark.service.AiLearningOs.Document_ingestion.OCRService ocrService) {
        this.ocrService = ocrService;
    }

    public String extract(MultipartFile file) throws Exception {
        // Nếu file là ảnh/pdf-scan: OCR
        // Nếu docx/pdf text: bạn có thể bổ sung parser riêng
        return ocrService.extractText(file);
    }
}
