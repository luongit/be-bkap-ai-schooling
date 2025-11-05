package com.bkap.aispark.controller;

import com.bkap.aispark.service.Json2VideoService;
import com.bkap.aispark.service.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
public class Json2VideoController {

    @Autowired
    private Json2VideoService json2VideoService;

    @Autowired
    private R2StorageService r2StorageService;

    /**
     * API mới: Upload ảnh + subtitle => upload ảnh lên R2 => render video từ ảnh
     */
    @PostMapping("/create")
    public ResponseEntity<?> createVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subtitle") String subtitle) {

        try {
            // 1️⃣ Upload ảnh lên Cloudflare R2
            String imageUrl = r2StorageService.uploadFile(file);

            // 2️⃣ Gọi JSON2VIDEO để render video từ ảnh + subtitle
            String videoUrl = json2VideoService.renderImageWithSubtitle(imageUrl, subtitle);

            // 3️⃣ Trả về kết quả cho FE
            return ResponseEntity.ok(Map.of(
                    "imageUrl", imageUrl,
                    "videoUrl", videoUrl
            ));

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi render video: " + e.getMessage()));
        }
    }
}
