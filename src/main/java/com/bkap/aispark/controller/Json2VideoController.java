package com.bkap.aispark.controller;

import com.bkap.aispark.dto.VideoBatchRequest;
import com.bkap.aispark.service.Json2VideoService;
import com.bkap.aispark.service.R2StorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/video")
public class Json2VideoController {

    @Autowired
    private Json2VideoService json2VideoService;

    @Autowired
    private R2StorageService r2StorageService;

    private final ObjectMapper mapper = new ObjectMapper();

    // ================= API upload ảnh + slides JSON + optional bg music file =================
    @PostMapping("/create-slides-advanced-upload")
    public ResponseEntity<?> createSlidesAdvancedUpload(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("slidesJson") String slidesJson,
            @RequestParam(value = "bgMusicFile", required = false) MultipartFile bgMusicFile
    ) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Danh sách files trống"));
            }

            // 1) Upload ảnh lên R2
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile file : files) {
                imageUrls.add(r2StorageService.uploadFile(file));
            }

            // 2) Parse slides JSON
            List<Map<String, Object>> slidesRaw = mapper.readValue(slidesJson, new TypeReference<>() {});
            List<VideoBatchRequest.Slide> slides = new ArrayList<>();

            double totalVideoDuration = 0.0;

            for (int i = 0; i < slidesRaw.size(); i++) {
                Map<String, Object> s = slidesRaw.get(i);
                VideoBatchRequest.Slide slide = new VideoBatchRequest.Slide();

                slide.setImageUrl(i < imageUrls.size() ? imageUrls.get(i) : null);

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

                // Lấy style
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

                // Thời lượng
                Double duration = null;
                if (s.get("durationSec") instanceof Number d1) duration = d1.doubleValue();
                else if (s.get("duration") instanceof Number d2) duration = d2.doubleValue();
                duration = (duration != null && duration > 0 ? duration : 12.0);
                slide.setDurationSec(duration);
                totalVideoDuration += duration;

                // Giọng đọc
                slide.setVoiceName((String) s.getOrDefault("voiceName", "vi-VN-NamMinhNeural"));

                slides.add(slide);
            }

            // 3) Upload bg music file nếu có
            String bgMusicUrl = null;
            if (bgMusicFile != null && !bgMusicFile.isEmpty()) {
                // optional validation here: extension, mime, size
                String name = bgMusicFile.getOriginalFilename() != null ? bgMusicFile.getOriginalFilename().toLowerCase() : "";
                if (!(name.endsWith(".mp3") || name.endsWith(".mpeg"))) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Chỉ chấp nhận mp3!"));
                }
                // upload to R2 -> get public url
                bgMusicUrl = r2StorageService.uploadFile(bgMusicFile);
            }

            // 4) Gọi service render video (chuyển cả totalVideoDuration)
            VideoBatchRequest req = new VideoBatchRequest();
            req.setSlides(slides);
            req.setBgMusicUrl(bgMusicUrl);

            String videoUrl = json2VideoService.renderSlideshowMultiTrack(req, totalVideoDuration);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "uploaded", files.size(),
                    "slides", slides.size(),
                    "videoUrl", videoUrl,
                    "bgMusicUrl", bgMusicUrl
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

    // ================= API nhận JSON trực tiếp (không upload file) =================
    @PostMapping("/create-slides-advanced")
    public ResponseEntity<?> createSlidesAdvanced(@RequestBody VideoBatchRequest req) {
        try {
            // nếu client đã gửi bgMusicUrl (public) thì service sẽ sử dụng
            // tính tổng duration nếu muốn
            double total = 0.0;
            if (req.getSlides() != null) {
                for (VideoBatchRequest.Slide s : req.getSlides()) {
                    if (s != null && s.getDurationSec() != null) total += s.getDurationSec();
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
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }

}
