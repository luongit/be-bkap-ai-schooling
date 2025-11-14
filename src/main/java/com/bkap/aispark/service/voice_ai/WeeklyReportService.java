package com.bkap.aispark.service.voice_ai;

import com.bkap.aispark.dto.voice_ai.WeeklyReportDTO;
import com.bkap.aispark.entity.voice_ai.VoiceTurn;
import com.bkap.aispark.repository.voice_ai.VoiceTurnRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class WeeklyReportService {

    private final VoiceTurnRepository repo;

    public WeeklyReportDTO generate(Long studentId, LocalDate start, LocalDate end) {

        Instant from = start.atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant to = end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

        List<VoiceTurn> turns = repo.findByStudentIdAndCreatedAtBetween(studentId, from, to);

        // Tổng ngày luyện tập
        int daysPracticed = (int) turns.stream()
                .map(t -> t.getCreatedAt().toString().substring(0, 10))
                .distinct().count();

        // Tổng phút = mỗi turn = 1.5–2 phút
        int totalMinutes = turns.size() * 2;

        int totalScenes = (int) turns.stream()
                .map(VoiceTurn::getSceneCode).distinct().count();

        int totalTurns = turns.size();

        // Trung bình điểm
        double avgPron = avg(turns, VoiceTurn::getPronunciationScore);
        double avgFlu = avg(turns, VoiceTurn::getFluencyScore);
        double avgInt = avg(turns, VoiceTurn::getIntonationScore);
        double avgConf = avg(turns, VoiceTurn::getConfidenceScore);

        WeeklyReportDTO dto = new WeeklyReportDTO();

        dto.setOverview(Map.of(
                "daysPracticed", daysPracticed,
                "totalMinutes", totalMinutes,
                "totalScenes", totalScenes,
                "totalTurns", totalTurns
        ));

        dto.setScores(Map.of(
                "avgPronunciation", avgPron,
                "avgFluency", avgFlu,
                "avgIntonation", avgInt,
                "avgConfidence", avgConf,
                "progress", Map.of(
                        "pronunciationDelta", 12,
                        "fluencyDelta", 9,
                        "intonationDelta", 6,
                        "confidenceDelta", 14
                )
        ));

        dto.setSounds(Map.of(
                "mastered", List.of(),
                "needPractice", List.of()
        ));

        dto.setNextWeekPlan(List.of(
                "2 scenes Shopping",
                "1 scene Asking for Directions",
                "1 Drill âm /v/ vs /w/"
        ));

        dto.setPraiseTemplate("Con đã nói rất tự tin! Tiếp tục duy trì phong độ nhé.");

        return dto;
    }

    private double avg(List<VoiceTurn> list, java.util.function.Function<VoiceTurn, Double> fn) {
        return list.stream()
                .map(fn)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }
}
