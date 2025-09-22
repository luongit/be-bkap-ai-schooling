package com.bkap.aispark.dto;

import java.time.LocalDate;

public class StudentGoalRequest {
    private String goal;
    private String subject;
    private String level;
    private String style;
    private String status;
    private LocalDate deadline;

    // Getters & Setters
    public String getGoal() { return goal; }
    public void setGoal(String goal) { this.goal = goal; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getLevel() { return level; }
    public void setLevel(String level) { this.level = level; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getDeadline() { return deadline; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
}
