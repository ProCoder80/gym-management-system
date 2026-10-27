package com.gym.model.dto;

import java.time.LocalDate;

public class EnrollmentForm {
    private String templateType = "BASIC";
    private Long memberId;
    private String planType = "MONTHLY";
    private String strategyName = "MonthlyFee";
    private String enrollmentType = "NEW"; // NEW or RENEWAL
    private Long trainerId;
    private boolean locker;
    private boolean dietPlan;
    private int ptSessions = 0;
    private LocalDate startDate = LocalDate.now();
    private String paymentMethod = "Credit Card";

    public EnrollmentForm() {
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
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

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(String enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    public boolean isLocker() {
        return locker;
    }

    public void setLocker(boolean locker) {
        this.locker = locker;
    }

    public boolean isDietPlan() {
        return dietPlan;
    }

    public void setDietPlan(boolean dietPlan) {
        this.dietPlan = dietPlan;
    }

    public int getPtSessions() {
        return ptSessions;
    }

    public void setPtSessions(int ptSessions) {
        this.ptSessions = ptSessions;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
