package com.bkap.aispark.security;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategorySafetyService {

    // Danh sách từ khóa cấm (có thể mở rộng)
    private static final List<String> bannedWords = List.of(
            "sex", "sexy", "sexual", "porn", "rape", "fuck", "18+",
            "tình dục", "dâm", "quan hệ", "kích dục",
            "bạo lực", "giết", "đánh nhau", "kill",
            "suicide", "tự tử", "tự sát",
            "ma túy", "thuốc lắc", "cần sa", "ketamine"
    );

    /** 
     * Kiểm tra danh mục có chứa từ khóa cấm không
     * Nếu chứa → ném lỗi
     */
    public void validate(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Tên danh mục không hợp lệ!");
        }

        String lower = name.toLowerCase();

        for (String bad : bannedWords) {
            if (lower.contains(bad)) {
                throw new RuntimeException("Tên danh mục không phù hợp với môi trường học đường.");
            }
        }
    }
}
