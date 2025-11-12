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

    // ========== [5️⃣.2️⃣ API NÂNG CAO CÓ UPLOAD ẢNH] ==========
    @PostMapping("/create-slides-advanced-upload")
    public ResponseEntity<?> createSlidesAdvancedUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("slidesJson") String slidesJson,
            @RequestParam(value = "bgMusicUrl", required = false) String bgMusicUrl
    ) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Danh sách files trống"));
            }

            // 🧩 1️⃣ Upload ảnh lên R2 và lưu URL
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                String url = r2StorageService.uploadFile(file);
                imageUrls.add(url);
            }

            // 🧩 2️⃣ Parse slidesJson từ FE
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            List<Map<String, Object>> slidesRaw = mapper.readValue(
                    slidesJson,
                    new com.fasterxml.jackson.core.type.TypeReference<>() {}
            );

            // 🧩 3️⃣ Ánh xạ sang DTO VideoBatchRequest.Slide
            List<VideoBatchRequest.Slide> slides = new ArrayList<>();

            for (int i = 0; i < slidesRaw.size(); i++) {
                Map<String, Object> s = slidesRaw.get(i);
                VideoBatchRequest.Slide slide = new VideoBatchRequest.Slide();

                // Ảnh tương ứng
                slide.setImageUrl((i < imageUrls.size()) ? imageUrls.get(i) : null);

                // 🧩 Lấy text từ texts[0].text
                String text = "";
                Object textsObj = s.get("texts");
                if (textsObj instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> t && t.get("text") != null) {
                        text = t.get("text").toString();
                    }
                }
                slide.setText(text);

                // 🧩 Lấy style từ texts[0].style
                Map<String, String> style = new LinkedHashMap<>();
                if (textsObj instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> t && t.get("style") instanceof Map<?, ?> styleMap) {
                        for (Map.Entry<?, ?> e : ((Map<?, ?>) t.get("style")).entrySet()) {
                            if (e.getKey() != null && e.getValue() != null)
                                style.put(e.getKey().toString(), e.getValue().toString());
                        }
                    }
                }
                slide.setStyle(style);

                // 🧩 Thời lượng
                Double duration = null;
                if (s.get("durationSec") instanceof Number d1)
                    duration = d1.doubleValue();
                else if (s.get("duration") instanceof Number d2)
                    duration = d2.doubleValue();
                slide.setDurationSec(duration != null && duration > 0 ? duration : 6.0);

                // 🧩 Giọng đọc (tạm mặc định)
                slide.setVoiceName((String) s.getOrDefault("voiceName", "vi-VN-HoaiMyNeural"));

                slides.add(slide);
            }

            // 🧩 4️⃣ Gọi service render video
            VideoBatchRequest req = new VideoBatchRequest();
            req.setSlides(slides);
            req.setBgMusicUrl(bgMusicUrl);

            String videoUrl = json2VideoService.renderSlideshowMultiTrack(req);

            // 🧩 5️⃣ Trả kết quả
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "uploaded", files.size(),
                    "slides", slides.size(),
                    "videoUrl", videoUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }


    // ========== [5️⃣ API NÂNG CAO: nhiều text, voice, nhạc nền, style riêng] ==========
    @PostMapping("/create-slides-advanced")
    public ResponseEntity<?> createSlidesAdvanced(@RequestBody VideoBatchRequest req) {
        try {
            String videoUrl = json2VideoService.renderSlideshowMultiTrack(req);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "slides", req.getSlides() == null ? 0 : req.getSlides().size(),
                    "videoUrl", videoUrl
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ========== [1️⃣ API TEST JSON BODY] ==========
    // Test nhanh bằng JSON body (Thunder/Postman)
    // Ví dụ body:
    // {
    //   "imageUrl": "https://.../img1.jpg",
    //   "subtitleText": "Hoàng hôn tĩnh lặng",
    //   "audioUrl": "https://.../voice.mp3"
    // }
//    @PostMapping("/create")
//    public ResponseEntity<?> createSingleByJson(@RequestBody VideoRequest req) {
//        try {
//            // Validate đầu vào
//            if (req.getImageUrl() == null || req.getSubtitleText() == null || req.getAudioUrl() == null) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu tham số imageUrl, subtitleText hoặc audioUrl"));
//            }
//
//            String videoUrl = json2VideoService.renderSingleSlide(
//                    req.getImageUrl(),
//                    req.getSubtitleText(),
//                    req.getAudioUrl()
//            );
//
//            return ResponseEntity.ok(Map.of(
//                    "imageUrl", req.getImageUrl(),
//                    "videoUrl", videoUrl
//            ));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
//        }
//    }

    // ========== [2️⃣ API TEST FORM UPLOAD] ==========
    // Test bằng FormData (React hoặc HTML form)
    // React example:
    // const fd = new FormData();
    // fd.append("file", selectedFile);
    // fd.append("subtitle", "Hoàng hôn tĩnh lặng");
    // fd.append("audioUrl", "https://.../voice.mp3");
    // await axios.post("/api/video/create-upload", fd);
//    @PostMapping("/create-upload")
//    public ResponseEntity<?> createSingleByUpload(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("subtitle") String subtitle,
//            @RequestParam("audioUrl") String audioUrl
//    ) {
//        try {
//            if (file.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "File ảnh không được để trống"));
//            }
//            if (subtitle == null || audioUrl == null) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Thiếu subtitle hoặc audioUrl"));
//            }
//
//            // 🧩 1. Upload ảnh lên R2 (Cloudflare)
//            String imageUrl = r2StorageService.uploadFile(file);
//
//            // 🧩 2. Render video
//            String videoUrl = json2VideoService.renderSingleSlide(imageUrl, subtitle, audioUrl);
//
//            // 🧩 3. Trả kết quả
//            return ResponseEntity.ok(Map.of(
//                    "imageUrl", imageUrl,
//                    "videoUrl", videoUrl
//            ));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi upload file: " + e.getMessage()));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi render video: " + e.getMessage()));
//        }
//    }
//
//    // ========== [3️⃣ API BATCH: nhiều ảnh + nhiều tiêu đề + 1 audio chung] ==========
//    @PostMapping("/create-batch")
//    public ResponseEntity<?> createBatchByJson(@RequestBody VideoBatchRequest req) {
//        try {
//            String videoUrl = json2VideoService.renderSlideshowTotal(
//                    req.getImages(),
//                    req.getTitles(),
//                    req.getAudioUrl()
//            );
//            return ResponseEntity.ok(Map.of(
//                    "count", req.getImages() == null ? 0 : req.getImages().size(),
//                    "videoUrl", videoUrl
//            ));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
//        }
//    }

    // ========== [4️⃣ API BATCH UPLOAD: nhiều ảnh upload từ React FormData] ==========
    // const fd = new FormData();
    // images.forEach(f => fd.append("files", f));
    // fd.append("titles", JSON.stringify(["Ảnh 1", "Ảnh 2", "Ảnh 3"]));
    // fd.append("audioUrl", "https://.../audio.mp3");
//    @PostMapping("/create-batch-upload")
//    public ResponseEntity<?> createBatchByUpload(
//            @RequestParam("files") List<MultipartFile> files,
//            @RequestParam("titles") String titlesJson,
//            @RequestParam("audioUrl") String audioUrl
//    ) {
//        try {
//            if (files == null || files.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of("error", "Danh sách files trống"));
//            }
//
//            // 🧩 1. Upload toàn bộ ảnh
//            List<String> imageUrls = new ArrayList<>();
//            for (MultipartFile f : files) {
//                imageUrls.add(r2StorageService.uploadFile(f));
//            }
//
//            // 🧩 2. Parse titles (["A","B","C"])
//            List<String> titles = parseJsonArrayOfStrings(titlesJson);
//
//            // 🧩 3. Render video
//            String videoUrl = json2VideoService.renderSlideshowTotal(imageUrls, titles, audioUrl);
//
//            // 🧩 4. Kết quả
//            return ResponseEntity.ok(Map.of(
//                    "uploaded", imageUrls.size(),
//                    "images", imageUrls,
//                    "videoUrl", videoUrl
//            ));
//
//        } catch (IOException e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi upload R2: " + e.getMessage()));
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError().body(Map.of("error", "Lỗi render: " + e.getMessage()));
//        }
//    }

    // ========== [Hàm hỗ trợ parse titles JSON] ==========
    private List<String> parseJsonArrayOfStrings(String json) {
        if (json == null || json.trim().isEmpty()) return Collections.emptyList();
        try {
            json = json.trim();
            if (json.startsWith("[")) json = json.substring(1);
            if (json.endsWith("]")) json = json.substring(0, json.length() - 1);
            if (json.trim().isEmpty()) return Collections.emptyList();

            // Cắt các phần tử mảng
            String[] parts = json.split("\\s*,\\s*");
            List<String> out = new ArrayList<>();
            for (String p : parts) {
                String s = p.trim();
                if (s.startsWith("\"")) s = s.substring(1);
                if (s.endsWith("\"")) s = s.substring(0, s.length() - 1);
                out.add(s);
            }
            return out;
        } catch (Exception ex) {
            throw new IllegalArgumentException("titles không phải mảng JSON hợp lệ", ex);
        }
    }
}
