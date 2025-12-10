package com.bkap.aispark.service.AiLearningOs;

import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

import com.bkap.aispark.entity.AiLearningOs.TeacherUploadedMaterial;
import com.bkap.aispark.repository.AiLearningOs.TeacherUploadedMaterialRepository;
import com.bkap.aispark.service.R2StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class MaterialService {

    @Autowired
    private TeacherUploadedMaterialRepository repo;

    // optional nếu lưu S3
    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private OCRService ocrService;

    @Autowired
    private AIProcessService aIProcessService;

    @Autowired
    private ObjectMapper mapper;

    public Long uploadMaterial(MultipartFile file, String title, String description, Long teacherId) throws Exception {

        // 1. Upload file lên S3 / local
        String fileUrl = r2StorageService.uploadFile(file);

        // 2. Xác định loại file
        String fileType = detectFileType(file.getOriginalFilename());

        // 3. Lưu record DB (chưa có AI data)
        TeacherUploadedMaterial m = new TeacherUploadedMaterial();
        m.setTeacherId(teacherId);
        m.setTitle(title);
        m.setDescription(description);
        m.setMaterialType(fileType);
        m.setFileUrl(fileUrl);

        TeacherUploadedMaterial saved = repo.save(m);

        // 4. Gửi file sang AI processing async
        processAIAsync(saved.getId(), file);

        return saved.getId();
    }

    private String detectFileType(String fileName) {
        if (fileName.endsWith(".pdf"))
            return "pdf";
        if (fileName.endsWith(".docx"))
            return "docx";
        if (fileName.endsWith(".png") || fileName.endsWith(".jpg"))
            return "image";
        return "unknown";
    }

    private Map<String, Object> buildMeta(MultipartFile file) throws Exception {
        Map<String, Object> meta = new HashMap<>();

        meta.put("file_name", file.getOriginalFilename());
        meta.put("file_size", file.getSize());
        meta.put("content_type", file.getContentType());

        // Nếu là PDF → lấy số trang
        if (file.getOriginalFilename().endsWith(".pdf")) {
            try (PDDocument doc = PDDocument.load(file.getInputStream())) {
                meta.put("page_count", doc.getNumberOfPages());
            }
        }

        // Nếu là ảnh → lấy width/height
        if (file.getContentType().startsWith("image")) {
            BufferedImage img = ImageIO.read(file.getInputStream());
            meta.put("width", img.getWidth());
            meta.put("height", img.getHeight());
        }

        meta.put("uploaded_at", System.currentTimeMillis());

        return meta;
    }

    @Async
    public void processAIAsync(Long materialId, MultipartFile file) {
        try {
            // 1. OCR or extract text
            String rawText = ocrService.extractText(file);

            // 2. Call AI model
            String summary = aIProcessService.generateSummary(rawText);
            String nodeGraph = aIProcessService.extractKnowledgeNodes(rawText);

            String cleanedJson = aIProcessService.cleanJson(nodeGraph);

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> nodeMap = mapper.readValue(cleanedJson, Map.class);
            Map<String, Object> meta = buildMeta(file);

            // 3. Update DB
            TeacherUploadedMaterial m = repo.findById(materialId).get();
            m.setMeta(meta);
            m.setRawText(rawText);
            m.setAiSummary(summary);
            m.setAiExtractedNodes(nodeMap);

            repo.save(m);
            System.out.println("=== AI PIPELINE FINISHED ===");

        } catch (Exception e) {
            System.err.println("AI PIPELINE FAILED for materialId = " + materialId);

            e.printStackTrace();
        }
    }
}
