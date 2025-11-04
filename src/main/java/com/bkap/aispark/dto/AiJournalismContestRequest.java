package com.bkap.aispark.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AiJournalismContestRequest {
    private String title;
    private String theme;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime submissionStart;
    private LocalDateTime submissionEnd;
    private String status; // optional

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDateTime getSubmissionStart() {
        return submissionStart;
    }

    public void setSubmissionStart(LocalDateTime submissionStart) {
        this.submissionStart = submissionStart;
    }

    public LocalDateTime getSubmissionEnd() {
        return submissionEnd;
    }

    public void setSubmissionEnd(LocalDateTime submissionEnd) {
        this.submissionEnd = submissionEnd;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Getters và Setters
}
