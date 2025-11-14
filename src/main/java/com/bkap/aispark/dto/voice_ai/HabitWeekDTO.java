package com.bkap.aispark.dto.voice_ai;

import lombok.Data;
import java.util.List;

@Data
public class HabitWeekDTO {
    private Long studentId;
    private String weekStart;
    private List<HabitDayDTO> days;
}
