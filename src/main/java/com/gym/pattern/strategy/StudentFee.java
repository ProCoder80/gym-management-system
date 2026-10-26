package com.gym.pattern.strategy;

import com.gym.model.entity.Membership;
import org.springframework.stereotype.Component;

/**
 * PATTERN: Strategy Pattern (Concrete Strategy)
 * 
 * Student Discount Fee calculation:
 * - Base Fee: $35.00 / month (30% student discount with valid ID)
 * - Locker add-on: $10.00
 * - Diet Plan add-on: $15.00
 * - Personal Training Sessions: $20.00 each
 */
@Component("StudentFee")
public class StudentFee implements FeeStrategy {

    public static final double BASE_FEE = 1000.0;
    public static final double LOCKER_FEE = 200.0;
    public static final double DIET_PLAN_FEE = 300.0;
    public static final double PT_SESSION_RATE = 350.0;

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
        return "StudentFee";
    }

    @Override
    public String getDescription() {
        return "Student Special: ₹1,000/mo (with valid Student ID). Full gym access at subsidized rates.";
    }

    @Override
    public double getBaseFee() {
        return BASE_FEE;
    }
}
