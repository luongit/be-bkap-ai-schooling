package com.bkap.aispark.service.voice_ai;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.bkap.aispark.dto.voice_ai.HabitDayDTO;
import com.bkap.aispark.dto.voice_ai.HabitWeekDTO;
import com.bkap.aispark.entity.voice_ai.VoiceTurn;
import com.bkap.aispark.repository.voice_ai.VoiceTurnRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HabitQuestService {

    private final VoiceTurnRepository repo;

    public HabitWeekDTO getWeekData(Long studentId, LocalDate weekStart) {

        List<HabitDayDTO> days = new ArrayList<>();
        int streak = 0;

        ZoneId zone = ZoneId.systemDefault();

        for (int i = 0; i < 7; i++) {

            LocalDate date = weekStart.plusDays(i);

            // -----------------------------------------------------
            // Convert LocalDate → Instant để phù hợp repository
            // -----------------------------------------------------
            Instant start = date.atStartOfDay(zone).toInstant();
            Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();

            List<VoiceTurn> list = repo.findInDay(studentId, start, end);

            // -----------------------------------------------------
            // Tính số scene, phút, streak
            // -----------------------------------------------------
            int scenesDone = (int) list.stream()
                    .map(VoiceTurn::getSceneCode)
                    .distinct()
                    .count();

            int minutes = list.size() * 2; // mỗi lượt ~ 2 phút

            if (scenesDone > 0) streak++;
            else streak = 0;

            // -----------------------------------------------------
            // Chest reward
            // -----------------------------------------------------
            String chest = null;
            if (streak == 3) chest = "SILVER";
            if (streak == 7) chest = "GOLD";

            // -----------------------------------------------------
            // Push DTO
            // -----------------------------------------------------
            days.add(
                HabitDayDTO.builder()
                        .date(date.toString())
                        .scenesDone(scenesDone)
                        .minutes(minutes)
                        .streak(streak)
                        .chestOpened(chest)
                        .build()
            );
        }

        // Build weekly DTO
        HabitWeekDTO dto = new HabitWeekDTO();
        dto.setStudentId(studentId);
        dto.setWeekStart(weekStart.toString());
        dto.setDays(days);

        return dto;
    }
}
