package com.gym.model.prototype;

/**
 * PATTERN: Prototype Pattern (Concrete Prototype 1: Basic)
 *
 * Predefined prototype template for standard monthly memberships:
 * - Base Fee: ₹1,500.00 / month
 * - Plan Type: MONTHLY
 * - Fee Strategy: MonthlyFee
 * - Add-ons: None enabled by default (locker=false, dietPlan=false, ptSessions=0)
 * - Mutable Perks: Standard gym floor access and day locker privileges
 */
public class BasicMembershipPrototype extends AbstractMembershipPrototype {

    public BasicMembershipPrototype() {
        this.templateName = "Basic";
        this.planType = "MONTHLY";
        this.feeStrategy = "MonthlyFee";
        this.baseFee = 1500.0;
        this.locker = false;
        this.dietPlan = false;
        this.ptSessions = 0;
        this.description = "Standard monthly fitness access: ₹1,500/mo. Flexible pay-as-you-go membership with full gym floor access.";

        this.features.add("Standard Gym Floor Access (06:00 AM - 10:00 PM)");
        this.features.add("Free Day-Use Locker Room Access");
        this.features.add("Complimentary Fitness Assessment & Orientation");
        this.features.add("Optional Add-Ons (Locker, Diet, PT) Configurable On-Demand");

        this.attributes.put("tier", "STANDARD");
        this.attributes.put("billingCycle", "MONTHLY");
        this.attributes.put("category", "INDIVIDUAL");
    }

    /**
     * Copy constructor for cloning.
     */
    public BasicMembershipPrototype(BasicMembershipPrototype source) {
        super(source);
    }

    @Override
    public BasicMembershipPrototype clonePrototype() {
        return new BasicMembershipPrototype(this);
    }
}
