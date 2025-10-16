package com.bkap.aispark.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class CreatePricingVersionRequest {

    @NotNull
    private Integer pricingId;

    @Min(0)
    private int tokenCost;

    @Min(0)
    private int creditCost;

    @Min(0)
    private int vndCost;

    // nếu null → áp dụng NOW()
    private LocalDateTime effectiveFrom;

    // tuỳ dùng: có bật gửi thông báo không
    private boolean notifyUsers;

    public Integer getPricingId() {
        return pricingId;
    }

    public void setPricingId(Integer pricingId) {
        this.pricingId = pricingId;
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

    public boolean isNotifyUsers() {
        return notifyUsers;
    }

    public void setNotifyUsers(boolean notifyUsers) {
        this.notifyUsers = notifyUsers;
    }
}
