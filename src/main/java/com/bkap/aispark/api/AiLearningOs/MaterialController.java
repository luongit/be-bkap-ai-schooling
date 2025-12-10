package com.bkap.aispark.api.AiLearningOs;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bkap.aispark.entity.AiLearningOs.TeacherUploadedMaterial;
import com.bkap.aispark.repository.AiLearningOs.TeacherUploadedMaterialRepository;
import com.bkap.aispark.service.AiLearningOs.MaterialService;

@RestController
@RequestMapping("/materials")
public class MaterialController {

    @Autowired
    private MaterialService materialService;

    @Autowired
    private TeacherUploadedMaterialRepository repo;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMaterial(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("teacher_id") Long teacherId,
            @RequestParam(value = "description", required = false) String description) {
        try {
            Long id = materialService.uploadMaterial(file, title, description, teacherId);
            return ResponseEntity.ok(Map.of(
                    "material_id", id,
                    "status", "uploaded"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/extract-text")
    public ResponseEntity<?> getExtractedText(@PathVariable Long id) {
        TeacherUploadedMaterial m = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        return ResponseEntity.ok(Map.of(
                "material_id", id,
                "raw_text", m.getRawText()));
    }
}
