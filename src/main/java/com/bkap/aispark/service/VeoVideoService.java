package com.bkap.aispark.service;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class VeoVideoService {

    @Value("${veo.api.generate.url}")
    private String generateUrl;

    @Value("${veo.api.status.url}")
    private String statusUrl;

    @Value("${veo.api.model.id}")
    private String modelId;

    private final List<String> apiKeys;
    private final AtomicInteger keyIndex = new AtomicInteger(0);
    private final OkHttpClient client;

    // --- Cấu hình Polling ---
    private static final int MAX_POLLING_ATTEMPTS = 60;
    private static final long POLLING_INTERVAL_MS = 5000; // 5 giây
    private static final MediaType JSON_TYPE = MediaType.parse("application/json");

    public VeoVideoService(@Value("${veo.api.keys}") String apiKeysStr) {
        this.apiKeys = Arrays.asList(apiKeysStr.split(","));
        // Cấu hình client với timeout dài cho tác vụ nặng
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS) // Tăng timeout cho quá trình chờ dài
                .writeTimeout(180, TimeUnit.SECONDS)
                .build();
        System.out.println("🔑 Loaded " + apiKeys.size() + " Veo keys for rotation.");
    }

    private String getNextKey() {
        int index = keyIndex.getAndUpdate(i -> (i + 1) % apiKeys.size());
        return apiKeys.get(index).trim();
    }

    private String getCurrentKey() {
        return apiKeys.get(keyIndex.get() % apiKeys.size()).trim();
    }

    /**
     * Hàm chính: Gửi yêu cầu sinh video, chờ Job hoàn thành, và trả về URL.
     */
    public String generateVideo(String prompt) {
        if (apiKeys.isEmpty()) {
            throw new RuntimeException("❌ Không tìm thấy API Key nào trong cấu hình!");
        }
        
        for (int i = 0; i < apiKeys.size(); i++) {
            String currentKey = getCurrentKey();
            try {
                String taskId = startVideoJob(prompt, currentKey);
                System.out.println("⏳ Task ID: " + taskId + " started with key: " + currentKey);
                String videoUrl = pollForResult(taskId, currentKey);
                System.out.println("✅ Video generated with key: " + currentKey);
                return videoUrl;
            } catch (RuntimeException e) {
                // Xử lý lỗi do Key hoặc Job thất bại
                System.err.println("❌ Key failed or job error with key: " + currentKey + " → " + e.getMessage());
                getNextKey();
            } catch (Exception e) {
                // Xử lý các lỗi I/O, Polling Timeout, v.v.
                System.err.println("❌ Unhandled error with key: " + currentKey + " → " + e.getMessage());
                getNextKey();
            }
        }
        throw new RuntimeException("❌ All keys failed or quota exceeded.");
    }

    /**
     * Bắt đầu Job sinh video và trả về Task ID.
     */
    private String startVideoJob(String prompt, String apiKey) throws IOException {
        JSONObject options = new JSONObject();
        // CẬP NHẬT: Đặt độ phân giải là 720p (mức tối thiểu API chấp nhận) để sửa lỗi 400
        options.put("aspectRatio", "16:9");
        options.put("resolution", "720p");

        JSONObject json = new JSONObject();
        json.put("model", modelId); 
        json.put("prompt", prompt);
        json.put("audio", false); 
        // THÊM THỜI GIAN VIDEO (MẶC ĐỊNH LÀ 5 GIÂY)
        json.put("duration", 3); 
        json.put("options", options);

        // FIX LỖI: Đổi thứ tự tham số thành create(MediaType, String)
        RequestBody body = RequestBody.create(JSON_TYPE, json.toString());

        Request request = new Request.Builder()
                .url(generateUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "Empty response body";

            if (!response.isSuccessful()) {
                int code = response.code();
                // Bổ sung kiểm tra lỗi 400 cho trường hợp request body sai định dạng
                if (code == 400 || code == 401 || code == 403 || code == 429 || responseBody.contains("Insufficient credits")) {
                    throw new RuntimeException("Key error, Bad Request or quota exceeded. Code: " + code + ". Body: " + responseBody);
                }
                throw new IOException("API error (" + code + "): " + responseBody);
            }

            JSONObject responseJson = new JSONObject(responseBody);
            
            // FIX LỖI: API trả về "taskId" (camelCase) chứ không phải "task_id" (snake_case)
            if (!responseJson.has("taskId")) { 
                throw new RuntimeException("Phản hồi thành công nhưng thiếu 'taskId'. Body: " + responseBody);
            }
            return responseJson.getString("taskId"); // Sửa key thành "taskId"
        }
    }

    /**
     * Polling (kiểm tra) trạng thái Job cho đến khi có URL video.
     */
    /**
     * Polling (kiểm tra) trạng thái Job cho đến khi có URL video.
     */
    private String pollForResult(String taskId, String apiKey) throws InterruptedException, IOException {
        long interval = POLLING_INTERVAL_MS;

        for (int attempt = 0; attempt < MAX_POLLING_ATTEMPTS; attempt++) {
            Thread.sleep(interval);

            // FIX LỖI 404: Nối Task ID vào đường dẫn thay vì dùng Query Parameter.
            // Ví dụ: statusUrl/veo_1760496358138_c2tvaa
            String url = statusUrl.endsWith("/") ? statusUrl + taskId : statusUrl + "/" + taskId;

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer " + apiKey)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                // ĐỌC BODY CHỈ MỘT LẦN
                String responseBody = response.body() != null ? response.body().string() : "Empty response body";
                
                if (!response.isSuccessful()) {
                    System.err.println("⚠️ Polling error (" + response.code() + "). Retrying... Body: " + responseBody);
                    // Có thể thêm logic kiểm tra lỗi 401/403 ở đây để đổi key ngay lập tức nếu cần
                    continue; // Tiếp tục vòng lặp Polling
                }

                JSONObject json = new JSONObject(responseBody);
                String status = json.optString("status", "unknown");

                System.out.println("🔄 Polling task " + taskId + ": " + status + " (" + (attempt + 1) + "/" + MAX_POLLING_ATTEMPTS + ")");

                if ("completed".equalsIgnoreCase(status)) {
                    // SỬA LỖI: Lấy "videoUrl" từ bên trong đối tượng "result"
                    if (json.has("result")) {
                        JSONObject resultJson = json.getJSONObject("result");
                        if (resultJson.has("videoUrl")) { 
                            System.out.println("🎉 Job completed. Video URL found.");
                            return resultJson.getString("videoUrl");
                        }
                    }
                    // Ném RuntimeException nếu cấu trúc JSON không đúng
                    throw new RuntimeException("Job hoàn thành nhưng thiếu 'result.videoUrl'. Body: " + responseBody);
                }

                if ("failed".equalsIgnoreCase(status) || "error".equalsIgnoreCase(status)) {
                    // Ném RuntimeException để kết thúc polling và kích hoạt key rotation (nếu cần)
                    throw new RuntimeException("Job failed: " + json.optString("error_message", "Unknown error") + ". Body: " + responseBody);
                }
            }
        }

        throw new RuntimeException("⏳ Timeout after " + (MAX_POLLING_ATTEMPTS * POLLING_INTERVAL_MS / 1000) + " seconds.");
    }
}
