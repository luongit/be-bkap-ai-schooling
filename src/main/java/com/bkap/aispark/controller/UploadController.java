package com.bkap.aispark.controller;

import com.bkap.aispark.service.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class UploadController {

    @Autowired
    private R2StorageService r2StorageService;

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String url = r2StorageService.uploadFile(file);
        return ResponseEntity.ok(Map.of("url", url));
    }
}
