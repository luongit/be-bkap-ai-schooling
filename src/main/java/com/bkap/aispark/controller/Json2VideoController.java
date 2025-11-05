package com.bkap.aispark.controller;


import com.bkap.aispark.dto.VideoBatchRequest;
import com.bkap.aispark.dto.VideoRequest;
import com.bkap.aispark.service.Json2VideoService;
import com.bkap.aispark.service.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/api/video")
public class Json2VideoController {

    @Autowired
    private Json2VideoService json2VideoService;

    @Autowired
    private R2StorageService r2StorageService;

    // upload 1 ảnh 1 tiêu đề ảnh
    @PostMapping("/create")
    public ResponseEntity<?> createSingleByJson(@RequestBody VideoRequest req) {
        try {
            String videoUrl = json2VideoService.renderSingleSlide(
                    req.getImageUrl(),
                    req.getSubtitleText(),
                    req.getAudioUrl()
            );
            return ResponseEntity.ok(Map.of(
                    "imageUrl", req.getImageUrl(),
                    "videoUrl", videoUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/create-upload")
    public ResponseEntity<?> createSingleByUpload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subtitle") String subtitle,
            @RequestParam("audioUrl") String audioUrl
    ) {
        try {
            String imageUrl = r2StorageService.uploadFile(file);
            String videoUrl = json2VideoService.renderSingleSlide(imageUrl, subtitle, audioUrl);
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


    @PostMapping("/create-batch")
    public ResponseEntity<?> createBatchByJson(@RequestBody VideoBatchRequest req) {
        try {
            String videoUrl = json2VideoService.renderSlideshowTotal(
                    req.getImages(),
                    req.getTitles(),
                    req.getAudioUrl()
            );
            return ResponseEntity.ok(Map.of(
                    "count", req.getImages() == null ? 0 : req.getImages().size(),
                    "videoUrl", videoUrl
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/create-batch-upload")
    public ResponseEntity<?> createBatchByUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("titles") String titlesJson,
            @RequestParam("audioUrl") String audioUrl
    ) {
        try {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile f : files) {
                imageUrls.add(r2StorageService.uploadFile(f));
            }

            List<String> titles = parseJsonArrayOfStrings(titlesJson);

            // gọi render
            String videoUrl = json2VideoService.renderSlideshowTotal(imageUrls, titles, audioUrl);

            return ResponseEntity.ok(Map.of(
                    "uploaded", imageUrls.size(),
                    "images", imageUrls,
                    "videoUrl", videoUrl
            ));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi upload R2: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi render: " + e.getMessage()));
        }
    }

    // khớp đơn giản cho titlesJson là JSON array string
    private List<String> parseJsonArrayOfStrings(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length()-1);
            if (json.trim().isEmpty()) return Collections.emptyList();
            String[] parts = json.split("\\s*,\\s*");
            List<String> out = new ArrayList<>();
            for (String p : parts) {
                String s = p.trim();
                if (s.startsWith("\"")) s = s.substring(1);
                if (s.endsWith("\"")) s = s.substring(0, s.length()-1);
                out.add(s);
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Tiêu đề không phải mảng json hợp lệ", ex);
        }
    }
}
