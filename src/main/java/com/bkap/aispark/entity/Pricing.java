package com.bkap.aispark.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "pricing")
public class Pricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "action_code", nullable = false, unique = true)
    private String actionCode;

    @Column(name = "action_name", nullable = false)
    private String actionName;

    @Column(name = "token_cost", nullable = false)
    private Integer tokenCost;

    @Column(name = "credit_cost", nullable = false)
    private Integer creditCost;

    @Column(name = "vnd_cost", nullable = false)
    private Integer vndCost;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    // getter/setter

}
