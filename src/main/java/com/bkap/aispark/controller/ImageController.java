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

    @PostMapping("/generate")
    @Async("openAIImage")  // <-- CHỈ ASYNC TẠI ĐÂY
    public CompletableFuture<ResponseEntity<Map<String,Object>>> generateImage(
            @RequestParam Long userId,
            @RequestParam String prompt,
            @RequestParam(defaultValue = "default") String style,
            @RequestParam(defaultValue = "1024x1024") String size
    ) {

        return CompletableFuture.supplyAsync(() -> {

            // 1) Check slot
            if (!libraryService.canStore(userId)) {
                return ResponseEntity.ok(Map.of(
                        "status", "LIMIT_REACHED",
                        "message", "Thư viện ảnh đã đầy. Vui lòng mua thêm dung lượng."
                ));
            }

            // 2) Check credit
            boolean ok = creditService.deductByAction(
                    userId,
                    "IMAGE_GENERATE",
                    "img-" + System.currentTimeMillis()
            );

            if (!ok) {
                return ResponseEntity.ok(Map.of(
                        "status", "NO_CREDIT",
                        "message", "Bạn không đủ credit để tạo ảnh."
                ));
            }

            // 3) Gọi AI (blocking nhưng chạy trong thread pool openAIImage)
            String finalUrl = imageService.generate(userId, prompt, style, size);

            

            // 4) Trả về FE
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "imageUrl", finalUrl
            ));

        }, CompletableFuture.delayedExecutor(0, java.util.concurrent.TimeUnit.MILLISECONDS));
        // 👆 forced use of the same pool (optional)
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(historyService.getHistory(userId));
    }
}

