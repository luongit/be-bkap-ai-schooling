package com.bkap.aispark.dto;

import java.time.LocalDateTime;

public class PricingVersionResponse {
    private Integer id;
    private Integer pricingId;
    private String actionCode;
    private String actionName;
    private int tokenCost;
    private int creditCost;
    private int vndCost;
    private LocalDateTime effectiveFrom;
    private boolean active;
    private Long createdBy;
    private LocalDateTime createdAt;

    // getters/setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPricingId() {
        return pricingId;
    }

    public void setPricingId(Integer pricingId) {
        this.pricingId = pricingId;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public int getTokenCost() {
        return tokenCost;
    }

    public void setTokenCost(int tokenCost) {
        this.tokenCost = tokenCost;
    }

    public int getCreditCost() {
        return creditCost;
    }

    public void setCreditCost(int creditCost) {
        this.creditCost = creditCost;
    }

    public int getVndCost() {
        return vndCost;
    }

    public void setVndCost(int vndCost) {
        this.vndCost = vndCost;
    }

    public LocalDateTime getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDateTime effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
