package com.bkap.aispark.controller;

import com.bkap.aispark.dto.ImageRequest;
import com.bkap.aispark.service.CreditService;
import com.bkap.aispark.service.ImageGenerationService;
import com.bkap.aispark.service.ImageLibraryService;
import com.bkap.aispark.service.UserImageHistoryService;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageGenerationService imageService;

    @Autowired
    private UserImageHistoryService historyService;

    @Autowired
    private CreditService creditService;

    @Autowired
    private ImageLibraryService libraryService;   

    public ImageController(ImageGenerationService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateImage(
            @RequestParam Long userId,
            @RequestParam String prompt,
            @RequestParam(defaultValue = "default") String style,
            @RequestParam(defaultValue = "1024x1024") String size
    ) {
        try {

            //  Kiểm tra dung lượng thư viện
            if (!libraryService.canStore(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "LIMIT_REACHED",
                        "message", "Thư viện ảnh đã đầy. Vui lòng mua thêm dung lượng."
                ));
            }

            //  Trừ credit
            boolean ok = creditService.deductByAction(
                    userId,
                    "IMAGE_GENERATE",
                    "img-" + System.currentTimeMillis()
            );

            if (!ok) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "NO_CREDIT",
                        "message", "Bạn không đủ credit để tạo ảnh."
                ));
            }

            //  Gọi AI tạo ảnh
            String finalUrl = imageService.generate(userId, prompt, style, size);

           

            //  Trả về ảnh
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "imageUrl", finalUrl
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "ERROR",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistory(@RequestParam Long userId) {
        return ResponseEntity.ok(historyService.getHistory(userId));
    }
}

