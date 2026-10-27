package com.gym.model.prototype;

import com.gym.model.dto.EnrollmentForm;
import com.gym.model.entity.Membership;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * PATTERN: Prototype Pattern (Prototype Interface)
 *
 * Intent:
 * Specify the kinds of objects to create using a prototypical instance,
 * and create new objects by copying this prototype.
 *
 * Responsibilities:
 * - Declares the cloning operation ({@link #clonePrototype()}) for membership plan templates.
 * - Provides accessors and mutators for common plan parameters (fee strategy, base fee, add-ons).
 * - Enforces member-specific data isolation between independent clone instances.
 * - Supports deep copy operations for nested mutable state (features and attributes).
 */
public interface MembershipPrototype extends Cloneable {

    /**
     * Creates and returns an independent copy (clone) of this membership prototype.
     * Implementations must perform a deep copy of any mutable collections or objects
     * to guarantee that modifications to the clone do not affect the original prototype
     * or any other clones.
     *
     * @return an independent, fully initialized copy of this prototype
     */
    MembershipPrototype clonePrototype();

    // Template identity
    String getTemplateName();
    void setTemplateName(String templateName);

    String getPlanType();
    void setPlanType(String planType);

    String getFeeStrategy();
    void setFeeStrategy(String feeStrategy);

    double getBaseFee();
    void setBaseFee(double baseFee);

    String getDescription();
    void setDescription(String description);

    // Default feature flags
    boolean isLocker();
    void setLocker(boolean locker);

    boolean isDietPlan();
    void setDietPlan(boolean dietPlan);

    int getPtSessions();
    void setPtSessions(int ptSessions);

    // Member-specific fields (independent between clones)
    Long getMemberId();
    void setMemberId(Long memberId);

    LocalDate getStartDate();
    void setStartDate(LocalDate startDate);

    Long getTrainerId();
    void setTrainerId(Long trainerId);

    // Deep-copied mutable structures
    List<String> getFeatures();
    void setFeatures(List<String> features);

    Map<String, String> getAttributes();
    void setAttributes(Map<String, String> attributes);

    /**
     * Converts this configured prototype instance into a validated {@link Membership}
     * entity using the existing {@link Membership.Builder} pattern.
     *
     * @return a valid Membership instance
     */
    Membership toMembership();

    /**
     * Prepopulates an {@link EnrollmentForm} DTO with this prototype's default configuration.
     *
     * @param form the target enrollment form
     */
    void applyToForm(EnrollmentForm form);
}
