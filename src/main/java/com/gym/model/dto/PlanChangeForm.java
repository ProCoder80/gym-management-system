package com.gym.model.dto;

public class PlanChangeForm {
    private Long memberId;
    private String newPlanType;
    private boolean locker;
    private boolean dietPlan;
    private int ptSessions = 0;

    public PlanChangeForm() {
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getNewPlanType() {
        return newPlanType;
    }

    public void setNewPlanType(String newPlanType) {
        this.newPlanType = newPlanType;
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
}
