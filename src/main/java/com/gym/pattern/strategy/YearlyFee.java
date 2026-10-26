package com.gym.pattern.strategy;

import com.gym.model.entity.Membership;
import org.springframework.stereotype.Component;

/**
 * PATTERN: Strategy Pattern (Concrete Strategy)
 * 
 * Yearly Fee calculation (Includes 20% commitment discount):
 * - Base Fee: $480.00 / year (equivalent to $40/mo instead of $50)
 * - Locker add-on: $120.00 / year ($10/mo discounted)
 * - Diet Plan add-on: $200.00 / year
 * - Personal Training Sessions: $25.00 each (bulk discount)
 */
@Component("YearlyFee")
public class YearlyFee implements FeeStrategy {

    public static final double BASE_FEE = 14400.0;
    public static final double LOCKER_FEE = 2400.0;
    public static final double DIET_PLAN_FEE = 4000.0;
    public static final double PT_SESSION_RATE = 400.0;

    @Override
    public double calculateFee(Membership membership) {
        if (membership == null) {
            return 0.0;
        }

        double fee = BASE_FEE;
        if (membership.isLocker()) {
            fee += LOCKER_FEE;
        }
        if (membership.isDietPlan()) {
            fee += DIET_PLAN_FEE;
        }
        if (membership.getPtSessions() > 0) {
            fee += (membership.getPtSessions() * PT_SESSION_RATE);
        }
        return Math.round(fee * 100.0) / 100.0;
    }

    @Override
    public String getStrategyName() {
        return "YearlyFee";
    }

    @Override
    public String getDescription() {
        return "Annual commitment: ₹14,400/yr (Save 20%). Includes complimentary orientation and priority slots.";
    }

    @Override
    public double getBaseFee() {
        return BASE_FEE;
    }
}
