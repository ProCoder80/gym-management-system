package com.gym.model.entity;

import java.time.LocalDateTime;

public class PlanHistoryEntry {
    private Long id;
    private Long memberId;
    private String actionType; // ENROLLMENT, UPGRADE, DOWNGRADE, UNDO
    private String planType;
    private double totalFee;
    private boolean locker;
    private boolean dietPlan;
    private int ptSessions;
    private String status;
    private String mementoData; // Serialized snapshot payload
    private LocalDateTime timestamp;

    public PlanHistoryEntry() {
    }

    public PlanHistoryEntry(Long id, Long memberId, String actionType, String planType, double totalFee,
                            boolean locker, boolean dietPlan, int ptSessions, String status,
                            String mementoData, LocalDateTime timestamp) {
        this.id = id;
        this.memberId = memberId;
        this.actionType = actionType;
        this.planType = planType;
        this.totalFee = totalFee;
        this.locker = locker;
        this.dietPlan = dietPlan;
        this.ptSessions = ptSessions;
        this.status = status;
        this.mementoData = mementoData;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMementoData() {
        return mementoData;
    }

    public void setMementoData(String mementoData) {
        this.mementoData = mementoData;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
