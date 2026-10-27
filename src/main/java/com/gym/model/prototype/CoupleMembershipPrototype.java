package com.gym.model.prototype;

/**
 * PATTERN: Prototype Pattern (Concrete Prototype 4: Couple)
 *
 * Predefined prototype template for dual/partner memberships:
 * - Base Fee: ₹2,500.00 / month (covers 2 people)
 * - Plan Type: COUPLE
 * - Fee Strategy: CoupleFee
 * - Add-ons: Dual lockers and joint diet consultation, 2 partner PT sessions bundled
 * - Mutable Perks: Dual access pass, dual lockers, couples meal guidance, 2 partner sessions
 */
public class CoupleMembershipPrototype extends AbstractMembershipPrototype {

    public CoupleMembershipPrototype() {
        this.templateName = "Couple";
        this.planType = "COUPLE";
        this.feeStrategy = "CoupleFee";
        this.baseFee = 2500.0;
        this.locker = true;
        this.dietPlan = true;
        this.ptSessions = 2;
        this.description = "Couple & Partner Package: ₹2,500/mo. All-inclusive dual pass with dual lockers, joint nutrition consultation, and 2 partner PT sessions.";

        this.features.add("Dual Pass (Full Gym Access for 2 Registered Partners)");
        this.features.add("Dual Private Secure Lockers Included");
        this.features.add("Joint Couple Nutrition & Meal Planning Included");
        this.features.add("2 Partner Personal Training Sessions Included");
        this.features.add("Unlimited Access to Strength, Cardio & Recovery Zones");

        this.attributes.put("tier", "COUPLE_PARTNER");
        this.attributes.put("billingCycle", "MONTHLY");
        this.attributes.put("category", "COUPLE");
        this.attributes.put("pax", "2");
    }

    /**
     * Copy constructor for cloning.
     */
    public CoupleMembershipPrototype(CoupleMembershipPrototype source) {
        super(source);
    }

    @Override
    public CoupleMembershipPrototype clonePrototype() {
        return new CoupleMembershipPrototype(this);
    }
}
