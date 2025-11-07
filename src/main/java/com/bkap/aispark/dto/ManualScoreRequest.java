package com.bkap.aispark.dto;

import java.math.BigDecimal;
import java.util.Map;

public class ManualScoreRequest {
    private BigDecimal totalScore;
    private String feedback;
    private Map<String, BigDecimal> criteriaJson;

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Map<String, BigDecimal> getCriteriaJson() {
        return criteriaJson;
    }

    public void setCriteriaJson(Map<String, BigDecimal> criteriaJson) {
        this.criteriaJson = criteriaJson;
    }
}
