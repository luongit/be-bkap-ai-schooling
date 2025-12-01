package com.bkap.aispark.controller;

import com.bkap.aispark.dto.VideoBatchRequest;
import com.bkap.aispark.entity.video_library_history.UserVideoHistory;
import com.bkap.aispark.entity.Pricing;
import com.bkap.aispark.service.CreditService;
import com.bkap.aispark.service.Json2VideoService;
import com.bkap.aispark.service.R2StorageService;
import com.bkap.aispark.service.video_library_history.UserVideoHistoryService;
import com.bkap.aispark.service.video_library_history.UserVideoLibraryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/video")
public class Json2VideoController {

    @Autowired
    private Json2VideoService json2VideoService;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private UserVideoHistoryService videoHistoryService;

    @Autowired
    private UserVideoLibraryService videoLibraryService;

    @Autowired
    private CreditService creditService;

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String ACTION_CODE = "VIDEO_CREATE";

    // ============================================================
    //  API CHÍNH: TẠO VIDEO + TRỪ CREDIT + TỰ LƯU VÀO THƯ VIỆN
    // ============================================================
    @PostMapping("/create-slides-advanced-upload")
    public ResponseEntity<?> createSlidesAdvancedUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("slidesJson") String slidesJson,
            @RequestParam(value = "bgMusicFile", required = false) MultipartFile bgMusicFile,
            @RequestParam("userId") Long userId
    ) {
        String refId = "video-" + System.currentTimeMillis();
        boolean deducted = false;
        int costFromPricing = 0;

        try {
            // ===== KIỂM TRA USER =====
            if (userId == null || userId <= 0) {
                return ResponseEntity.status(401).body(Map.of("error", "Thiếu hoặc sai userId!"));
            }

            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Danh sách files trống"));
            }

            // ===== LẤY GIÁ VIDEO TỪ pricing DB =====
            Pricing pricing = creditService.getPricing(ACTION_CODE);
            costFromPricing = pricing.getCreditCost();

            // ===== TRỪ CREDIT =====
            deducted = creditService.deductByAction(userId, ACTION_CODE, refId);

            if (!deducted) {
                return ResponseEntity.status(402).body(Map.of(
                        "status", "NO_CREDIT",
                        "error", "Không đủ credit để tạo video!",
                        "required", costFromPricing
                ));
            }

            // ===== UPLOAD ẢNH LÊN R2 =====
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                imageUrls.add(r2StorageService.uploadFile(file));
            }

            // ===== PARSE SLIDES JSON =====
            List<Map<String, Object>> slidesRaw =
                    mapper.readValue(slidesJson, new TypeReference<>() {});

            List<VideoBatchRequest.Slide> slides = new ArrayList<>();
            double totalVideoDuration = 0.0;

            for (int i = 0; i < slidesRaw.size(); i++) {
                Map<String, Object> s = slidesRaw.get(i);

                VideoBatchRequest.Slide slide = new VideoBatchRequest.Slide();
                slide.setImageUrl(imageUrls.get(i));

                // Lấy text
                String text = "";
                Object textsObj = s.get("texts");
                if (textsObj instanceof List<?> list && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof Map<?, ?> t && t.get("text") != null) {
                        text = t.get("text").toString();
                    }
                }
                slide.setText(text);

                // Thời lượng
                Double duration = null;
                if (s.get("durationSec") instanceof Number d1) duration = d1.doubleValue();
                duration = (duration != null && duration > 0 ? duration : 12.0);
                slide.setDurationSec(duration);
                totalVideoDuration += duration;

                // Voice
                slide.setVoiceName((String) s.getOrDefault("voiceName", "vi-VN-NamMinhNeural"));

                slides.add(slide);
            }

            // ===== UPLOAD NHẠC NỀN =====
            String bgMusicUrl = null;
            if (bgMusicFile != null && !bgMusicFile.isEmpty()) {
                String name = bgMusicFile.getOriginalFilename().toLowerCase();
                if (!name.endsWith(".mp3") && !name.endsWith(".m4a") && !name.endsWith(".mpeg")) {
                    return ResponseEntity.badRequest().body(Map.of("error", "File nhạc phải là MP3 hoặc M4A"));
                }
                bgMusicUrl = r2StorageService.uploadFile(bgMusicFile);
            }

            // ===== TẠO VIDEO =====
            VideoBatchRequest req = new VideoBatchRequest();
            req.setSlides(slides);
            req.setBgMusicUrl(bgMusicUrl);

            String videoUrl = json2VideoService.renderSlideshowMultiTrack(req, totalVideoDuration);

            // ===== LƯU LỊCH SỬ VIDEO =====
            String thumbnailUrl = imageUrls.get(0);

            String title = slides.stream()
                    .map(VideoBatchRequest.Slide::getText)
                    .filter(t -> t != null && !t.trim().isEmpty())
                    .findFirst()
                    .orElse("Video AI " + LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));

            UserVideoHistory history = new UserVideoHistory(userId, videoUrl, thumbnailUrl, title);
            videoHistoryService.save(history);

            videoLibraryService.incrementUsed(userId);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "videoUrl", videoUrl,
                    "thumbnailUrl", thumbnailUrl,
                    "title", title,
                    "savedToLibrary", true
            ));

        } catch (Exception e) {

            // ===== REFUND =====
            if (deducted) {
                try {
                    creditService.addCredit(
                            userId,
                            costFromPricing,
                            "refund",
                            "Refund do lỗi tạo video",
                            "refund-" + refId
                    );
                } catch (Exception ignored) {}
            }

            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Lỗi tạo video: " + e.getMessage()));
        }
    }

    // ============================================================
    //  API CŨ: KHÔNG UPLOAD FILE
    // ============================================================
    @PostMapping("/create-slides-advanced")
    public ResponseEntity<?> createSlidesAdvanced(@RequestBody VideoBatchRequest req) {
        try {
            double total = 0.0;
            if (req.getSlides() != null) {
                for (VideoBatchRequest.Slide s : req.getSlides()) {
                    if (s != null && s.getDurationSec() != null) {
                        total += s.getDurationSec();
                    }
                }
            }
            String videoUrl = json2VideoService.renderSlideshowMultiTrack(req, total);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "slides", req.getSlides() == null ? 0 : req.getSlides().size(),
                    "videoUrl", videoUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
