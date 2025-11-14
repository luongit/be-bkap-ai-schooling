package com.bkap.aispark.dto.voice_ai;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HabitDayDTO {
    private String date;
    private int scenesDone;
    private int minutes;
    private int streak;
    private String chestOpened; // null / SILVER / GOLD
}
