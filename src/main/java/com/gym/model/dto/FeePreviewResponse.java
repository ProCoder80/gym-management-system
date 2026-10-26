package com.gym.model.dto;

public class FeePreviewResponse {
    private String planType;
    private String strategyName;
    private double baseFee;
    private double lockerFee;
    private double dietPlanFee;
    private double ptSessionsFee;
    private double subtotal;
    private double discount;
    private double totalFee;
    private String description;

    public FeePreviewResponse() {
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public double getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(double baseFee) {
        this.baseFee = baseFee;
    }

    public double getLockerFee() {
        return lockerFee;
    }

    public void setLockerFee(double lockerFee) {
        this.lockerFee = lockerFee;
    }

    public double getDietPlanFee() {
        return dietPlanFee;
    }

    public void setDietPlanFee(double dietPlanFee) {
        this.dietPlanFee = dietPlanFee;
    }

    public double getPtSessionsFee() {
        return ptSessionsFee;
    }

    public void setPtSessionsFee(double ptSessionsFee) {
        this.ptSessionsFee = ptSessionsFee;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
