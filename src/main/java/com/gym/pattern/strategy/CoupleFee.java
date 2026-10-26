package com.gym.pattern.strategy;

import com.gym.model.entity.Membership;
import org.springframework.stereotype.Component;

/**
 * PATTERN: Strategy Pattern (Concrete Strategy)
 * 
 * Couple / Partner Fee calculation:
 * - Base Fee: $80.00 / month (covers 2 people)
 * - Locker add-on: $25.00 (dual lockers)
 * - Diet Plan add-on: $40.00 (joint nutrition plan)
 * - Personal Training Sessions: $45.00 each (dual partner sessions)
 */
@Component("CoupleFee")
public class CoupleFee implements FeeStrategy {

    public static final double BASE_FEE = 2500.0;
    public static final double LOCKER_FEE = 500.0;
    public static final double DIET_PLAN_FEE = 800.0;
    public static final double PT_SESSION_RATE = 800.0;

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
        return "CoupleFee";
    }

    @Override
    public String getDescription() {
        return "Couple & Partner: ₹2,500/mo. All-inclusive access for pairs with joint nutrition & workout perks.";
    }

    @Override
    public double getBaseFee() {
        return BASE_FEE;
    }
}
