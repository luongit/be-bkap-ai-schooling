package com.bkap.aispark.controller;

import com.bkap.aispark.service.CreditService;
import com.bkap.aispark.service.ImageGenerationService;
import com.bkap.aispark.service.ImageLibraryService;
import com.bkap.aispark.service.UserImageHistoryService;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageGenerationService imageService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private ImageLibraryService libraryService;

    @Autowired
    private UserImageHistoryService historyService;

    // ========================
    //  GENERATE IMAGE (ASYNC)
    // ========================
    @PostMapping("/generate")
    @Async("imageExecutor")
    public CompletableFuture<ResponseEntity<Map<String,Object>>> generateImage(
            @RequestParam Long userId,
            @RequestParam String prompt,
            @RequestParam(defaultValue = "default") String style,
            @RequestParam(defaultValue = "1024x1024") String size
    ) {

        try {
            // 0) Kiểm tra slot thư viện ảnh
            if (!libraryService.canStore(userId)) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.ok(Map.of(
                            "status", "LIMIT_REACHED",
                            "message", "Thư viện ảnh đã đầy. Vui lòng mua thêm dung lượng."
                    ))
                );
            }

            // 1) Trừ credit
            boolean ok = creditService.deductByAction(
                    userId,
                    "IMAGE_GENERATE",
                    "img-" + System.currentTimeMillis()
            );

            if (!ok) {
                return CompletableFuture.completedFuture(
                    ResponseEntity.ok(Map.of(
                            "status", "NO_CREDIT",
                            "message", "Bạn không đủ credit để tạo ảnh."
                    ))
                );
            }

            // 2) Generate ảnh
            String finalUrl = imageService.generate(userId, prompt, style, size);

            return CompletableFuture.completedFuture(
                ResponseEntity.ok(Map.of(
                        "status", "success",
                        "imageUrl", finalUrl
                ))
            );

        } catch (Exception e) {

            return CompletableFuture.completedFuture(
                ResponseEntity.ok(Map.of(
                        "status", "ERROR",
                        "message", e.getMessage()
                ))
            );
        }
    }


    // ======================
    //  GET IMAGE HISTORY
    // ======================
    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(historyService.getHistory(userId));
    }
}
