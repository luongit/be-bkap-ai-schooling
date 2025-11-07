package com.bkap.aispark.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiJournalismContestRequest {
    private String title;
    private String theme;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime submissionStart;
    private LocalDateTime submissionEnd;
    private String status; // optional
    private Double totalScore;

    private List<RubricDto> rubrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RubricDto {
        private String criterion;
        private String description;
        private Double weight;
    }

}
