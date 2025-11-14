package com.bkap.aispark.controller.voice_ai;

import com.bkap.aispark.entity.voice_ai.VoiceTurn;
import com.bkap.aispark.service.voice_ai.VoiceTurnService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/voice-gpt5/report")
@CrossOrigin(origins = "http://localhost:3000")
public class VoiceReportController {

    @Autowired
    private VoiceTurnService voiceTurnService;

    /** Báo cáo tuần hiện tại của 1 học sinh */
    @GetMapping("/{studentId}/weekly")
    public ResponseEntity<?> weeklyReport(@PathVariable Long studentId) {
        // Lấy tuần hiện tại theo Asia/Bangkok
        ZoneId zone = ZoneId.of("Asia/Bangkok");
        LocalDate today = LocalDate.now(zone);
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        return rangeReport(studentId,
                weekStart.atStartOfDay(zone).toInstant(),
                weekEnd.plusDays(1).atStartOfDay(zone).toInstant());
    }

    /** Báo cáo theo khoảng thời gian tuỳ chọn */
    @GetMapping("/{studentId}/range")
    public ResponseEntity<?> rangeReport(
            @PathVariable Long studentId,
            @RequestParam("from")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam("to")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        List<VoiceTurn> turns = voiceTurnService.getHistoryBetween(studentId, from, to);
        if (turns.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "studentId", studentId,
                    "totalTurns", 0,
                    "message", "No data in range"
            ));
        }

        // Nếu đã có trường điểm => tính trung bình; nếu chưa có, vẫn trả meta
        Double avgPron = avg(turns.stream().map(VoiceTurn::getPronunciationScore).collect(Collectors.toList()));
        Double avgFlu  = avg(turns.stream().map(VoiceTurn::getFluencyScore).collect(Collectors.toList()));
        Double avgInt  = avg(turns.stream().map(VoiceTurn::getIntonationScore).collect(Collectors.toList()));
        Double avgCon  = avg(turns.stream().map(VoiceTurn::getConfidenceScore).collect(Collectors.toList()));

        Set<String> scenes = turns.stream().map(VoiceTurn::getSceneCode).collect(Collectors.toCollection(LinkedHashSet::new));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("studentId", studentId);
        summary.put("totalTurns", turns.size());
        summary.put("avgPronunciation", avgPron);
        summary.put("avgFluency", avgFlu);
        summary.put("avgIntonation", avgInt);
        summary.put("avgConfidence", avgCon);
        summary.put("topScenes", scenes);
        summary.put("recommendation", recommendNext(avgPron));

        return ResponseEntity.ok(summary);
    }

    private Double avg(List<Double> xs) {
        List<Double> v = xs.stream().filter(Objects::nonNull).toList();
        if (v.isEmpty()) return null;
        return v.stream().mapToDouble(d -> d).average().orElse(0);
    }

    private String recommendNext(Double avgPron) {
        if (avgPron == null) return "Tiếp tục luyện 5 scene/tuần. Tập trung 1–2 âm phổ biến.";
        if (avgPron >= 85) return "Tuyệt! Tuần tới nâng độ khó: thêm scene Shopping (linking) & Small Talk (intonation).";
        if (avgPron >= 60) return "Giữ nhịp! Tuần tới thêm Focus Drill âm /θ/ và /v/ vs /w/ (3 phút/ngày).";
        return "Bắt đầu nhẹ nhàng: luyện chậm 3 scene cơ bản + 1 drill kết thúc âm /t/ mỗi ngày.";
    }
}
