package com.bkap.aispark.controller;

import com.bkap.aispark.dto.ImageRequest;
import com.bkap.aispark.service.ImageGenerationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    private final ImageGenerationService imageService;

    public ImageController(ImageGenerationService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/generate")
    public ResponseEntity<?> generateImage(@RequestBody ImageRequest request) {
        String result = imageService.generateAndSaveImage(
                request.getUserId(),
                request.getPrompt(),
                request.getStyle(),
                request.getSize()
        );

        if (result.startsWith("ERROR:")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
        }

        return ResponseEntity.ok(result); // trả về ảnh base64
    }
}
