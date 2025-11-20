package com.bkap.aispark.service;


import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.image.CreateImageRequest;
import com.theokanning.openai.image.Image;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Duration;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ImageGenerationService {

    private final OpenAiService openai; // 👈 Singleton
    private final R2StorageService r2;
    private final UserImageHistoryService history;
    private final ImageLibraryService libraryService;

    public ImageGenerationService(
            R2StorageService r2,
            UserImageHistoryService history,
            ImageLibraryService libraryService,
            @Value("${openai.api.key}") String apiKey
    ) {
        this.r2 = r2;
        this.history = history;
        this.libraryService = libraryService;

        this.openai = new OpenAiService(apiKey, Duration.ofSeconds(60)); // 👈 only once
    }

    public String generate(Long userId, String prompt, String style, String size) {
        try {

            // Kiểm tra slot
            if (!libraryService.canStore(userId)) {
                throw new RuntimeException("LIMIT_REACHED");
            }

            // Gọi OpenAI
            CreateImageRequest request = CreateImageRequest.builder()
                    .model("dall-e-3")
                    .prompt(prompt)
                    .size(size)
                    .responseFormat("b64_json")
                    .n(1)
                    .build();

            List<Image> images = openai.createImage(request).getData();

            // Nếu trả về list rỗng → không tăng slot
            if (images == null || images.isEmpty() || images.get(0).getB64Json() == null) {
                throw new RuntimeException("EMPTY_IMAGE");
            }

            String base64 = images.get(0).getB64Json();
            byte[] bytes = Base64.getDecoder().decode(base64);

            FakeMultipartFile fakeFile = new FakeMultipartFile(
                    UUID.randomUUID() + ".png",
                    "image/png",
                    bytes
            );

            String finalUrl = r2.uploadFile(fakeFile);

            // ---- CHỈ TĂNG SLOT Ở ĐÂY ----
            libraryService.incrementUsed(userId);

            // Lưu lịch sử
            history.save(userId, prompt, style, size, finalUrl, "SUCCESS", null);

            return finalUrl;


         } catch (Exception e) {

            String msg = e.getMessage();

            //  Lỗi safety của OpenAI
            if (msg != null && msg.contains("safety system")) {
                String safeError = "Tạo ảnh thất bại vì yêu cầu chứa nội dung bị hạn chế. "
                        + "Vui lòng mô tả lại theo cách lành mạnh và không mang tính bạo lực hoặc nhạy cảm.";
                history.save(userId, prompt, style, size, null, "ERROR", safeError);
                throw new RuntimeException(safeError);
            }

            //  Lỗi timeout, mạng, kết nối
            if (msg != null && (msg.contains("timed out") || msg.contains("timeout") || msg.contains("503"))) {
                String timeoutError = "Hệ thống AI đang quá tải hoặc mất kết nối. Vui lòng thử lại sau vài giây.";
                history.save(userId, prompt, style, size, null, "ERROR", timeoutError);
                throw new RuntimeException(timeoutError);
            }

            //  Các lỗi khác
            String generic = "Đã xảy ra lỗi khi tạo ảnh. Chi tiết: " + e.getMessage();
            history.save(userId, prompt, style, size, null, "ERROR", generic);
            throw new RuntimeException(generic);
        }

}
}
