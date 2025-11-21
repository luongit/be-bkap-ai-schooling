package com.bkap.aispark.controller.video_library_history;

import com.bkap.aispark.service.CreditService;
import com.bkap.aispark.service.video_library_history.UserVideoHistoryService;
import com.bkap.aispark.service.video_library_history.UserVideoLibraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/video/library")
public class VideoLibraryApi {

    private final UserVideoLibraryService libraryService;
    private final UserVideoHistoryService historyService;
    private final CreditService creditService;

    public VideoLibraryApi(UserVideoLibraryService libraryService,
                           UserVideoHistoryService historyService,
                           CreditService creditService) {
        this.libraryService = libraryService;
        this.historyService = historyService;
        this.creditService = creditService;
    }

    // Lấy thông tin dung lượng (used / capacity)
    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestParam Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId không hợp lệ"));
        }
        try {
            return ResponseEntity.ok(libraryService.getOrCreate(userId));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Lỗi hệ thống khi lấy thông tin thư viện"));
        }
    }

    // Lấy danh sách video trong lịch sử
    @GetMapping("")
    public ResponseEntity<?> list(@RequestParam Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId không hợp lệ"));
        }
        try {
            return ResponseEntity.ok(historyService.getHistory(userId));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Không thể tải danh sách video"));
        }
    }

    // Mua thêm 5 slot lưu trữ
    @PostMapping("/extend")
    public ResponseEntity<?> extend(@RequestParam Long userId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId không hợp lệ"));
        }

        // Trừ credit
        boolean deducted = creditService.deductByAction(userId, "VIDEO_LIBRARY_SLOT", "extend-" + System.currentTimeMillis());
        if (!deducted) {
            return ResponseEntity.status(402).body(Map.of(
                    "error", "Không đủ credit để mua thêm slot!",
                    "action", "VIDEO_LIBRARY_SLOT"
            ));
        }

        // Tăng slot
        libraryService.increaseCapacity(userId);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Đã mua thêm 5 slot video thành công!",
                "addedSlots", 5
        ));
    }

    // Xóa video khỏi thư viện
    @DeleteMapping("/delete")
    public ResponseEntity<?> delete(@RequestParam Long userId, @RequestParam Long videoId) {
        if (userId == null || userId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "userId không hợp lệ"));
        }
        if (videoId == null || videoId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "videoId không hợp lệ"));
        }

        boolean deleted = historyService.delete(userId, videoId);
        if (deleted) {
            libraryService.decrementUsed(userId); // Giảm số video đã dùng
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã xóa video thành công",
                    "freedSlot", true
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Video không tồn tại hoặc đã bị xóa trước đó"
            ));
        }
    }
}