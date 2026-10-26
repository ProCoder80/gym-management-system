package com.gym.model.entity;

import java.time.LocalDate;

/**
 * PATTERN: Builder Pattern
 * 
 * Intent:
 * Separates the construction of a complex Membership object from its representation,
 * allowing the same construction process to create various representations.
 * 
 * In this implementation:
 * - Required fields: memberId, planType, startDate (enforced at build() validation).
 * - Optional fields: trainerId, locker, dietPlan, ptSessions.
 * - Validation logic is centralized inside Membership.Builder.build(), ensuring that
 *   no invalid Membership instances can ever be instantiated in the application.
 */
public class Membership {

    private Long id;
    private Long memberId;
    private String planType;       // MONTHLY, YEARLY, STUDENT, COUPLE
    private String feeStrategy;    // MonthlyFee, YearlyFee, StudentFee, CoupleFee
    private double baseFee;
    private double totalFee;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long trainerId;        // Optional assigned personal trainer
    private boolean locker;        // Optional locker add-on
    private boolean dietPlan;      // Optional nutrition / diet plan add-on
    private int ptSessions;        // Optional personal training session count
    private String status;         // ACTIVE, EXPIRED, CANCELLED

    // Private constructor: Instances must be created via Builder or internal DAO mapping
    public Membership() {
    }

    private Membership(Builder builder) {
        this.memberId = builder.memberId;
        this.planType = builder.planType;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.trainerId = builder.trainerId;
        this.locker = builder.locker;
        this.dietPlan = builder.dietPlan;
        this.ptSessions = builder.ptSessions;
        this.feeStrategy = builder.feeStrategy;
        this.baseFee = builder.baseFee;
        this.totalFee = builder.totalFee;
        this.status = builder.status != null ? builder.status : "ACTIVE";
    }

    // =========================================================================
    // BUILDER PATTERN IMPLEMENTATION
    // =========================================================================
    public static class Builder {
        // Required fields
        private final Long memberId;
        private final String planType;
        private final LocalDate startDate;

        // Optional fields with defaults
        private LocalDate endDate;
        private Long trainerId = null;
        private boolean locker = false;
        private boolean dietPlan = false;
        private int ptSessions = 0;
        private String feeStrategy = "MonthlyFee";
        private double baseFee = 0.0;
        private double totalFee = 0.0;
        private String status = "ACTIVE";

        /**
         * Builder constructor enforcing the REQUIRED fields.
         *
         * @param memberId  ID of the member (Required, > 0)
         * @param planType  Plan type e.g. MONTHLY, YEARLY, STUDENT, COUPLE (Required, non-empty)
         * @param startDate Starting date of membership (Required, non-null)
         */
        public Builder(Long memberId, String planType, LocalDate startDate) {
            this.memberId = memberId;
            this.planType = planType;
            this.startDate = startDate;
        }

        // Optional chained configuration methods
        public Builder trainer(Long trainerId) {
            this.trainerId = trainerId;
            return this;
        }

        public Builder locker(boolean locker) {
            this.locker = locker;
            return this;
        }

        public Builder dietPlan(boolean dietPlan) {
            this.dietPlan = dietPlan;
            return this;
        }

        public Builder ptSessions(int ptSessions) {
            this.ptSessions = ptSessions;
            return this;
        }

        public Builder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder feeStrategy(String feeStrategy) {
            this.feeStrategy = feeStrategy;
            return this;
        }

        public Builder baseFee(double baseFee) {
            this.baseFee = baseFee;
            return this;
        }

        public Builder totalFee(double totalFee) {
            this.totalFee = totalFee;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Validates all required inputs and business constraints before returning
         * a fully constructed, valid Membership object.
         * 
         * @return Validated Membership instance
         * @throws IllegalStateException if any required constraint is violated
         */
        public Membership build() {
            // Validation 1: Required memberId
            if (memberId == null || memberId <= 0) {
                throw new IllegalStateException("Validation Failed: memberId is required and must be a positive integer.");
            }

            // Validation 2: Required planType
            if (planType == null || planType.trim().isEmpty()) {
                throw new IllegalStateException("Validation Failed: planType is required and cannot be blank.");
            }

            // Validation 3: Required startDate
            if (startDate == null) {
                throw new IllegalStateException("Validation Failed: startDate is required and cannot be null.");
            }

            // Validation 4: Optional ptSessions cannot be negative
            if (ptSessions < 0) {
                throw new IllegalStateException("Validation Failed: ptSessions cannot be negative (provided: " + ptSessions + ").");
            }

            // Automatically derive endDate if not explicitly set
            if (endDate == null) {
                String cleanPlan = planType.toUpperCase().trim();
                if ("YEARLY".equals(cleanPlan)) {
                    this.endDate = startDate.plusYears(1);
                } else {
                    this.endDate = startDate.plusMonths(1);
                }
            }

            return new Membership(this);
        }
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================
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

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getFeeStrategy() {
        return feeStrategy;
    }

    public void setFeeStrategy(String feeStrategy) {
        this.feeStrategy = feeStrategy;
    }

    public double getBaseFee() {
        return baseFee;
    }

    public void setBaseFee(double baseFee) {
        this.baseFee = baseFee;
    }

    public double getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(double totalFee) {
        this.totalFee = totalFee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Membership{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", planType='" + planType + '\'' +
                ", feeStrategy='" + feeStrategy + '\'' +
                ", totalFee=" + totalFee +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", trainerId=" + trainerId +
                ", locker=" + locker +
                ", dietPlan=" + dietPlan +
                ", ptSessions=" + ptSessions +
                ", status='" + status + '\'' +
                '}';
    }
}
