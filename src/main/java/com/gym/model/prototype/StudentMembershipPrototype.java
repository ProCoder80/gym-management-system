package com.gym.model.prototype;

/**
 * PATTERN: Prototype Pattern (Concrete Prototype 3: Student)
 *
 * Predefined prototype template for student discounted memberships:
 * - Base Fee: ₹1,000.00 / month (30% subsidized rate)
 * - Plan Type: STUDENT
 * - Fee Strategy: StudentFee
 * - Add-ons: None enabled by default (locker=false, dietPlan=false, ptSessions=0)
 * - Mutable Perks: Full equipment access, group classes with valid student ID
 */
public class StudentMembershipPrototype extends AbstractMembershipPrototype {

    public StudentMembershipPrototype() {
        this.templateName = "Student";
        this.planType = "STUDENT";
        this.feeStrategy = "StudentFee";
        this.baseFee = 1000.0;
        this.locker = false;
        this.dietPlan = false;
        this.ptSessions = 0;
        this.description = "Student Special: ₹1,000/mo (30% discount with valid Student ID). Full gym access at subsidized rates.";

        this.features.add("Subsidized Student Rate (30% Off Standard Monthly Rate)");
        this.features.add("Full Gym Equipment & Cardio Floor Access");
        this.features.add("Standard Group Fitness Classes Included");
        this.features.add("Valid College/University Student ID Required at Check-in");

        this.attributes.put("tier", "STUDENT_SUBSIDIZED");
        this.attributes.put("billingCycle", "MONTHLY");
        this.attributes.put("category", "STUDENT");
        this.attributes.put("idRequired", "true");
    }

    /**
     * Copy constructor for cloning.
     */
    public StudentMembershipPrototype(StudentMembershipPrototype source) {
        super(source);
    }

    @Override
    public StudentMembershipPrototype clonePrototype() {
        return new StudentMembershipPrototype(this);
    }
}
