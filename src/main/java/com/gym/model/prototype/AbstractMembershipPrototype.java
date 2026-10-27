package com.gym.model.prototype;

import com.gym.model.dto.EnrollmentForm;
import com.gym.model.entity.Membership;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PATTERN: Prototype Pattern (Abstract Prototype Base Class)
 *
 * Provides shared fields, copy-constructor-based deep copy mechanics,
 * and integration with {@link Membership.Builder} and {@link EnrollmentForm}.
 */
public abstract class AbstractMembershipPrototype implements MembershipPrototype {

    protected String templateName;
    protected String planType;
    protected String feeStrategy;
    protected double baseFee;
    protected String description;
    protected boolean locker;
    protected boolean dietPlan;
    protected int ptSessions;

    // Member-specific fields: unassigned in templates, customized on clones
    protected Long memberId;
    protected LocalDate startDate;
    protected Long trainerId;

    // Mutable collections demonstrating strict deep copying
    protected List<String> features = new ArrayList<>();
    protected Map<String, String> attributes = new HashMap<>();

    public AbstractMembershipPrototype() {
    }

    /**
     * Copy Constructor for creating a DEEP COPY of the prototype.
     * Ensures that nested collections and mutable objects are cloned independently.
     *
     * @param source the original prototype to copy
     */
    protected AbstractMembershipPrototype(AbstractMembershipPrototype source) {
        if (source != null) {
            this.templateName = source.templateName;
            this.planType = source.planType;
            this.feeStrategy = source.feeStrategy;
            this.baseFee = source.baseFee;
            this.description = source.description;
            this.locker = source.locker;
            this.dietPlan = source.dietPlan;
            this.ptSessions = source.ptSessions;

            // Member-specific fields: copied as-is, can be mutated independently
            this.memberId = source.memberId;
            this.startDate = source.startDate;
            this.trainerId = source.trainerId;

            // DEEP COPY: Re-instantiate mutable collections
            this.features = source.features != null ? new ArrayList<>(source.features) : new ArrayList<>();
            this.attributes = source.attributes != null ? new HashMap<>(source.attributes) : new HashMap<>();
        }
    }

    @Override
    public abstract MembershipPrototype clonePrototype();

    @Override
    public MembershipPrototype clone() {
        return clonePrototype();
    }

    @Override
    public Membership toMembership() {
        LocalDate date = this.startDate != null ? this.startDate : LocalDate.now();
        Long memId = this.memberId != null && this.memberId > 0 ? this.memberId : 1L;

        return new Membership.Builder(memId, this.planType, date)
                .trainer(this.trainerId)
                .locker(this.locker)
                .dietPlan(this.dietPlan)
                .ptSessions(this.ptSessions)
                .feeStrategy(this.feeStrategy)
                .baseFee(this.baseFee)
                .status("ACTIVE")
                .build();
    }

    @Override
    public void applyToForm(EnrollmentForm form) {
        if (form == null) return;
        form.setTemplateType(this.templateName);
        form.setPlanType(this.planType);
        form.setStrategyName(this.feeStrategy);
        form.setLocker(this.locker);
        form.setDietPlan(this.dietPlan);
        form.setPtSessions(this.ptSessions);
        if (this.trainerId != null) {
            form.setTrainerId(this.trainerId);
        }
        if (this.startDate != null) {
            form.setStartDate(this.startDate);
        }
    }

    // =========================================================================
    // Getters and Setters
    // =========================================================================

    @Override
    public String getTemplateName() {
        return templateName;
    }

    @Override
    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    @Override
    public String getPlanType() {
        return planType;
    }

    @Override
    public void setPlanType(String planType) {
        this.planType = planType;
    }

    @Override
    public String getFeeStrategy() {
        return feeStrategy;
    }

    @Override
    public void setFeeStrategy(String feeStrategy) {
        this.feeStrategy = feeStrategy;
    }

    @Override
    public double getBaseFee() {
        return baseFee;
    }

    @Override
    public void setBaseFee(double baseFee) {
        this.baseFee = baseFee;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean isLocker() {
        return locker;
    }

    @Override
    public void setLocker(boolean locker) {
        this.locker = locker;
    }

    @Override
    public boolean isDietPlan() {
        return dietPlan;
    }

    @Override
    public void setDietPlan(boolean dietPlan) {
        this.dietPlan = dietPlan;
    }

    @Override
    public int getPtSessions() {
        return ptSessions;
    }

    @Override
    public void setPtSessions(int ptSessions) {
        this.ptSessions = ptSessions;
    }

    @Override
    public Long getMemberId() {
        return memberId;
    }

    @Override
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    @Override
    public LocalDate getStartDate() {
        return startDate;
    }

    @Override
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    @Override
    public Long getTrainerId() {
        return trainerId;
    }

    @Override
    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
    }

    @Override
    public List<String> getFeatures() {
        return features;
    }

    @Override
    public void setFeatures(List<String> features) {
        this.features = features != null ? new ArrayList<>(features) : new ArrayList<>();
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "templateName='" + templateName + '\'' +
                ", planType='" + planType + '\'' +
                ", feeStrategy='" + feeStrategy + '\'' +
                ", baseFee=" + baseFee +
                ", locker=" + locker +
                ", dietPlan=" + dietPlan +
                ", ptSessions=" + ptSessions +
                ", memberId=" + memberId +
                ", startDate=" + startDate +
                ", trainerId=" + trainerId +
                '}';
    }
}
