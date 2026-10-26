package com.gym.pattern.strategy;

import com.gym.model.entity.Membership;

/**
 * PATTERN: Strategy Pattern
 * 
 * Intent:
 * Defines a family of algorithms, encapsulates each one, and makes them interchangeable.
 * Strategy lets the algorithm vary independently from clients that use it.
 * 
 * In this Gym Management System:
 * - Fee calculation strategies (MonthlyFee, YearlyFee, StudentFee, CoupleFee) encapsulate
 *   different pricing models, discounts, locker rates, and personal training session rates.
 * - Selection is executed dynamically via FeeStrategyFactory using a Map lookup,
 *   completely eliminating messy 'if/else' or 'switch' ladders.
 */
public interface FeeStrategy {

    /**
     * Calculates the total fee for a given membership based on strategy-specific rates
     * and optional add-ons (locker, diet plan, personal training sessions).
     *
     * @param membership The membership containing plan configuration and add-ons
     * @return Calculated total fee in dollars
     */
    double calculateFee(Membership membership);

    /**
     * Returns the unique strategy key/name (e.g. "MonthlyFee", "YearlyFee").
     *
     * @return Strategy identifier
     */
    String getStrategyName();

    /**
     * Returns a human-friendly description of the pricing terms and discounts.
     *
     * @return Strategy description
     */
    String getDescription();

    /**
     * Returns the base subscription fee before add-ons.
     *
     * @return Base fee amount
     */
    double getBaseFee();
}
