package com.bkap.aispark.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.bkap.aispark.service.VeoVideoService; // Đã đổi sang VeoVideoService

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller để xử lý các yêu cầu liên quan đến sinh video.
 * Endpoint: /videos/generate-video (Giả định base API URL đã được cấu hình bên ngoài)
 * Chịu trách nhiệm gọi VeoVideoService để bắt đầu và chờ kết quả video.
 */
@RestController
@RequestMapping("/api/videos") // Đã đổi base path thành /videos
public class VideoController {

    private final VeoVideoService veoVideoService; // Đã đổi service name

    // Spring sẽ tự động tiêm (inject) VeoVideoService vào đây
    public VideoController(VeoVideoService veoVideoService) { // Đã đổi service name
        this.veoVideoService = veoVideoService;
    }

    /**
     * Xử lý yêu cầu POST để sinh video từ prompt.
     * Quá trình này là blocking (chờ đợi) vì dịch vụ backend phải poll trạng thái Job.
     * @param prompt Mô tả văn bản cho video (được truyền qua query parameter).
     * @return ResponseEntity chứa videoUrl nếu thành công, hoặc thông báo lỗi nếu thất bại.
     */
    @PostMapping("/generate-video")
    public ResponseEntity<Map<String, String>> generateVideo(
            @RequestParam String prompt,
            // Header Authorization được sử dụng để xác thực, nhưng logic được giả định
            // là xử lý bởi một lớp filter/interceptor khác trong ứng dụng thực tế.
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        
        // *LƯU Ý QUAN TRỌNG: Trong ứng dụng thực tế, logic kiểm tra và trừ Credit 
        // sẽ được đặt tại đây hoặc trong một Interceptor trước khi gọi Service.
        
        try {
            // Gọi service để bắt đầu job và chờ (poll) kết quả.
            String videoUrl = veoVideoService.generateVideo(prompt); // Đã gọi đúng service

            // Phản hồi thành công (200 OK) với URL video
            Map<String, String> response = new HashMap<>();
            response.put("videoUrl", videoUrl);

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            // Xử lý các lỗi RuntimeException từ service (ví dụ: key lỗi, timeout, job thất bại)
            System.err.println("Video generation failed: " + e.getMessage());
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi tạo video: " + e.getMessage());

            // Trả về lỗi 500 INTERNAL_SERVER_ERROR hoặc 400 BAD_REQUEST nếu có thể phân biệt rõ hơn
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
            
        } catch (Exception e) {
            // Bắt lỗi bất ngờ khác (ví dụ: lỗi JSON parsing)
            System.err.println("Unexpected error during video generation: " + e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Lỗi không xác định: Vui lòng kiểm tra log server.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
