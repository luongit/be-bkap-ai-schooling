package com.bkap.aispark.dto.voice_ai;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TurnResponse {

    // ====== PHẢN HỒI CHÍNH ======
    // Câu góp ý / chỉnh sửa chính (thường tiếng Anh)
    private String replyText;

    // Giải thích tiếng Việt cho học sinh
    private String replyVietnamese;

    // Câu động viên, khích lệ
    private String praise;

    // ====== AUDIO ======
    // Audio Base64 trả về cho FE phát
    private String audioBase64;

    // ====== THÔNG TIN BÀI TẬP ======
    private Integer turnIndex;              // Lượt thứ mấy trong scene
    private String tipKey;                  // Khóa mẹo phát âm (sound_...)

    // ====== ĐIỂM SỐ TỪ MODULE PHÂN TÍCH ======
    // pronunciation, fluency, intonation, confidence
    private Map<String, Double> scores;

    // ====== TỔNG KẾT SCENE (để dùng sau) ======
    private boolean sceneCompleted;         // Đã hoàn thành scene chưa
    private String summary;                 // Tóm tắt nhận xét cuối scene
    private Map<String, Double> avgScores;  // Điểm trung bình scene
}
