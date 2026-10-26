package com.gym.pattern.strategy;

import com.gym.model.entity.Membership;
import org.springframework.stereotype.Component;

/**
 * PATTERN: Strategy Pattern (Concrete Strategy)
 * 
 * Standard Monthly Fee calculation:
 * - Base Fee: $50.00 / month
 * - Locker add-on: $15.00
 * - Diet Plan add-on: $25.00
 * - Personal Training Sessions: $30.00 each
 */
@Component("MonthlyFee")
public class MonthlyFee implements FeeStrategy {

    public static final double BASE_FEE = 50.0;
    public static final double LOCKER_FEE = 15.0;
    public static final double DIET_PLAN_FEE = 25.0;
    public static final double PT_SESSION_RATE = 30.0;

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
        return "MonthlyFee";
    }

    @Override
    public String getDescription() {
        return "Standard monthly rate: $50/mo. Flexible, pay-as-you-go membership with full gym access.";
    }

    @Override
    public double getBaseFee() {
        return BASE_FEE;
    }
}
