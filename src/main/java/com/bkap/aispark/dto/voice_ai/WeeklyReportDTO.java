package com.bkap.aispark.dto.voice_ai;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class WeeklyReportDTO {

    private Long studentId;

    private String rangeStart;
    private String rangeEnd;

    private Map<String, Object> overview;

    private Map<String, Object> scores;

    private Map<String, Object> sounds;

    private List<String> nextWeekPlan;

    private String praiseTemplate;
}
