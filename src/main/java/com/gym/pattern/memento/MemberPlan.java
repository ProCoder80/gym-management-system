package com.gym.pattern.memento;

import java.time.LocalDateTime;

/**
 * PATTERN: Memento Pattern (Originator)
 * 
 * Intent:
 * Without violating encapsulation, captures and externalizes an object's internal state
 * so that the object can be restored to this state later.
 * 
 * Roles in this system:
 * - Originator: MemberPlan creates a Memento containing a snapshot of its current plan configuration
 *   and uses the Memento to restore its internal state.
 * - Memento: MemberPlan.Memento stores internal state (planType, totalFee, locker, dietPlan, ptSessions, status).
 *   Protected from manipulation by external callers.
 * - Caretaker: PlanManagementService and PlanHistoryDAO manage the persistence stack in the SQLite plan_history table.
 */
public class MemberPlan {

    private Long memberId;
    private String planType;
    private double totalFee;
    private boolean locker;
    private boolean dietPlan;
    private int ptSessions;
    private String status;
    private LocalDateTime lastModified;

    public MemberPlan() {
        this.lastModified = LocalDateTime.now();
    }

    public MemberPlan(Long memberId, String planType, double totalFee, boolean locker, boolean dietPlan, int ptSessions, String status) {
        this.memberId = memberId;
        this.planType = planType;
        this.totalFee = totalFee;
        this.locker = locker;
        this.dietPlan = dietPlan;
        this.ptSessions = ptSessions;
        this.status = status;
        this.lastModified = LocalDateTime.now();
    }

    /**
     * MEMENTO PATTERN: Originator creates a Memento snapshot containing its current state.
     *
     * @return Immutable Memento instance
     */
    public Memento saveToMemento() {
        return new Memento(this.planType, this.totalFee, this.locker, this.dietPlan, this.ptSessions, this.status, LocalDateTime.now());
    }

    /**
     * MEMENTO PATTERN: Originator restores its state from a previously saved Memento snapshot.
     *
     * @param memento The Memento to restore state from
     */
    public void restoreFromMemento(Memento memento) {
        if (memento == null) {
            throw new IllegalArgumentException("Cannot restore from a null Memento.");
        }
        this.planType = memento.getPlanType();
        this.totalFee = memento.getTotalFee();
        this.locker = memento.isLocker();
        this.dietPlan = memento.isDietPlan();
        this.ptSessions = memento.getPtSessions();
        this.status = memento.getStatus();
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Upgrades the member plan with new parameters.
     */
    public void upgrade(String newPlanType, double newFee, boolean locker, boolean dietPlan, int ptSessions) {
        this.planType = newPlanType;
        this.totalFee = newFee;
        this.locker = locker;
        this.dietPlan = dietPlan;
        this.ptSessions = ptSessions;
        this.lastModified = LocalDateTime.now();
    }

    /**
     * Downgrades the member plan with new parameters.
     */
    public void downgrade(String newPlanType, double newFee, boolean locker, boolean dietPlan, int ptSessions) {
        this.planType = newPlanType;
        this.totalFee = newFee;
        this.locker = locker;
        this.dietPlan = dietPlan;
        this.ptSessions = ptSessions;
        this.lastModified = LocalDateTime.now();
    }

    // =========================================================================
    // INNER MEMENTO CLASS
    // =========================================================================
    /**
     * PATTERN: Memento Pattern (Memento Object)
     * Stores immutable snapshot of MemberPlan state.
     */
    public static class Memento {
        private final String planType;
        private final double totalFee;
        private final boolean locker;
        private final boolean dietPlan;
        private final int ptSessions;
        private final String status;
        private final LocalDateTime timestamp;

        public Memento(String planType, double totalFee, boolean locker, boolean dietPlan,
                       int ptSessions, String status, LocalDateTime timestamp) {
            this.planType = planType;
            this.totalFee = totalFee;
            this.locker = locker;
            this.dietPlan = dietPlan;
            this.ptSessions = ptSessions;
            this.status = status;
            this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        }

        public String getPlanType() {
            return planType;
        }

        public double getTotalFee() {
            return totalFee;
        }

        public boolean isLocker() {
            return locker;
        }

        public boolean isDietPlan() {
            return dietPlan;
        }

        public int getPtSessions() {
            return ptSessions;
        }

        public String getStatus() {
            return status;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        @Override
        public String toString() {
            return "Memento{" +
                    "planType='" + planType + '\'' +
                    ", totalFee=" + totalFee +
                    ", locker=" + locker +
                    ", dietPlan=" + dietPlan +
                    ", ptSessions=" + ptSessions +
                    ", status='" + status + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================
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

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }
}
