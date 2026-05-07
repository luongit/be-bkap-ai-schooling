package com.bkap.aispark.service.teach;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ExtractionProgressService {
    
    // Lưu trữ tiến trình theo id bài giảng (Từ 0 đến 100)
    private final ConcurrentHashMap<Long, Integer> progressMap = new ConcurrentHashMap<>();

    // Cập nhật phần trăm
    public void updateProgress(Long lessonId, int percentage) {
        progressMap.put(lessonId, percentage);
    }

    // Lấy phần trăm hiện tại (-1 nghĩa là không có tiến trình nào)
    public int getProgress(Long lessonId) {
        return progressMap.getOrDefault(lessonId, -1);
    }

    // Xóa tiến trình khi đã hoàn thành
    public void finishProgress(Long lessonId) {
        progressMap.remove(lessonId);
    }
}