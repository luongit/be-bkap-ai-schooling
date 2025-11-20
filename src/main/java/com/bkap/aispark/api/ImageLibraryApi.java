package com.bkap.aispark.api;

import com.bkap.aispark.service.ImageLibraryService;
import com.bkap.aispark.service.UserImageHistoryService;
import com.bkap.aispark.service.CreditService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/images/library")
public class ImageLibraryApi {

    private final ImageLibraryService libraryService;
    private final UserImageHistoryService historyService;
    private final CreditService creditService;

    public ImageLibraryApi(
            ImageLibraryService libraryService,
            UserImageHistoryService historyService,
            CreditService creditService
    ) {
        this.libraryService = libraryService;
        this.historyService = historyService;
        this.creditService = creditService;
    }
    
    @Autowired
    private RestTemplate restTemplate;

    // 🟢 Lấy thông tin dung lượng thư viện
    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestParam Long userId) {
        return ResponseEntity.ok(libraryService.getOrCreate(userId));
    }

    // 🟢 Lấy toàn bộ ảnh trong thư viện
    @GetMapping("")
    public ResponseEntity<?> listImages(@RequestParam Long userId) {
        return ResponseEntity.ok(historyService.getHistory(userId));
    }

    // 🟢 Mua thêm 5 slot dung lượng
    @PostMapping("/extend")
    public ResponseEntity<?> extendLibrary(@RequestParam Long userId) {

        boolean ok = creditService.deductByAction(
                userId,
                "IMAGE_LIBRARY_SLOT",
                "extend-" + System.currentTimeMillis()
        );

        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "NO_CREDIT",
                    "message", "Không đủ credit để mua thêm dung lượng."
            ));
        }

        libraryService.increaseCapacity(userId);

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Đã mua thêm 5 slot thư viện."
        ));
    }

    // 🟢 Xoá ảnh trong thư viện
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteImage(
            @RequestParam Long userId,
            @RequestParam Long imageId
    ) {
    	boolean shouldDecrement = historyService.deleteImage(userId, imageId);

    	if (!shouldDecrement && shouldDecrement != false) {
    	    return ResponseEntity.badRequest().body("Không thể xoá ảnh!");
    	}

    	// ❗ chỉ decrement nếu cần
    	if (shouldDecrement) {
    	    libraryService.decrementUsed(userId);
    	}

    	return ResponseEntity.ok("Đã xoá ảnh.");

}
    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadImage(@RequestParam String url) {
        try {
            byte[] fileBytes = restTemplate.getForObject(url, byte[].class);

            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=ai-image.png")
                .header("Content-Type", "image/png")
                .body(fileBytes);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

}

