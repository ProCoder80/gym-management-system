package com.gym.model.prototype;

/**
 * PATTERN: Prototype Pattern (Concrete Prototype 2: Premium)
 *
 * Predefined prototype template for all-inclusive annual memberships:
 * - Base Fee: ₹14,400.00 / year (20% commitment discount)
 * - Plan Type: YEARLY
 * - Fee Strategy: YearlyFee
 * - Add-ons: All premium add-ons bundled by default (locker=true, dietPlan=true, ptSessions=5)
 * - Mutable Perks: 24/7 VIP privileges, dedicated locker, diet consultation, 5 PT sessions
 */
public class PremiumMembershipPrototype extends AbstractMembershipPrototype {

    public PremiumMembershipPrototype() {
        this.templateName = "Premium";
        this.planType = "YEARLY";
        this.feeStrategy = "YearlyFee";
        this.baseFee = 14400.0;
        this.locker = true;
        this.dietPlan = true;
        this.ptSessions = 5;
        this.description = "All-inclusive VIP annual pass: ₹14,400/yr (Save 20%). Includes dedicated locker, nutrition counseling, and 5 PT sessions.";

        this.features.add("24/7 Unlimited Access to All Gym Zones & Equipment");
        this.features.add("Dedicated Secure Private Locker Included");
        this.features.add("Custom Nutrition Consultation & Macro Diet Plan Included");
        this.features.add("5 Dedicated 1-on-1 Personal Training Sessions Included");
        this.features.add("Priority Group Fitness Class Reservations");
        this.features.add("Complimentary Sauna & Steam Bath Privileges");

        this.attributes.put("tier", "VIP_PREMIUM");
        this.attributes.put("billingCycle", "YEARLY");
        this.attributes.put("category", "INDIVIDUAL_VIP");
        this.attributes.put("commitmentMonths", "12");
    }

    /**
     * Copy constructor for cloning.
     */
    public PremiumMembershipPrototype(PremiumMembershipPrototype source) {
        super(source);
    }

    @Override
    public PremiumMembershipPrototype clonePrototype() {
        return new PremiumMembershipPrototype(this);
    }
}
