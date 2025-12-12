package com.bkap.aispark.service.AiLearningOs.Document_ingestion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bkap.aispark.entity.AiLearningOs.Document_ingestion.AiMaterialExtraction;
import com.bkap.aispark.repository.AiLearningOs.Document_ingestion.AiMaterialExtractionRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AiMaterialExtractionService {

    @Autowired
    private AiMaterialExtractionRepository extractionRepository;

    // Lưu một extraction
    public AiMaterialExtraction saveExtraction(AiMaterialExtraction extraction) {
        return extractionRepository.save(extraction);
    }

    // Lấy extraction theo ID
    public Optional<AiMaterialExtraction> getExtractionById(Long id) {
        return extractionRepository.findById(id);
    }

    // Chia tài liệu thành các đoạn nhỏ
    public List<AiMaterialExtraction> extractChunks(AiMaterialExtraction extraction) {
        String rawText = extraction.getRawText();
        int chunkSize = 250; // Độ dài mỗi chunk (ví dụ 250 ký tự)
        List<AiMaterialExtraction> chunks = new ArrayList<>();

        // Chia rawText thành các đoạn nhỏ
        for (int i = 0; i < rawText.length(); i += chunkSize) {
            String chunkText = rawText.substring(i, Math.min(i + chunkSize, rawText.length()));
            AiMaterialExtraction chunk = new AiMaterialExtraction();
            chunk.setMaterialId(extraction.getMaterialId());
            chunk.setRawText(chunkText);
            chunk.setChunkIndex(i / chunkSize + 1);  // Cập nhật chỉ số của chunk
            chunks.add(extractionRepository.save(chunk)); // Lưu vào cơ sở dữ liệu
        }

        return chunks;
    }
}
