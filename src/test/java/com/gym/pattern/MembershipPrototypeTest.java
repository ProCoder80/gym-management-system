package com.gym.pattern;

import com.gym.model.dto.EnrollmentForm;
import com.gym.model.entity.Membership;
import com.gym.model.prototype.BasicMembershipPrototype;
import com.gym.model.prototype.CoupleMembershipPrototype;
import com.gym.model.prototype.MembershipPrototype;
import com.gym.model.prototype.MembershipPrototypeRegistry;
import com.gym.model.prototype.PremiumMembershipPrototype;
import com.gym.model.prototype.StudentMembershipPrototype;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PATTERN TEST: Prototype Design Pattern Verification
 *
 * Verifies the 10 requirements of the Prototype Pattern implementation:
 * 1. Independent cloning via clonePrototype().
 * 2. Predefined templates: Basic, Premium, Student, and Couple.
 * 3. Startup registration in MembershipPrototypeRegistry.
 * 4. Retrieval and cloning prior to customization.
 * 5. Sensible defaults aligned with business model.
 * 6. Member-specific data (memberId, startDate, trainerId) independence between clones.
 * 7. Strict deep copying of mutable nested objects/collections (features, attributes).
 * 8. Rejection of unknown template keys with meaningful exceptions.
 * 9. Registry singleton efficiency (Map lookup without per-request re-instantiation).
 * 10. Clean integration with Membership.Builder and EnrollmentForm.
 */
class MembershipPrototypeTest {

    private MembershipPrototypeRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MembershipPrototypeRegistry();
    }

    @Test
    @DisplayName("Prototype: BasicMembershipPrototype default configuration is valid")
    void testBasicMembershipPrototypeDefaults() {
        BasicMembershipPrototype basic = new BasicMembershipPrototype();
        assertEquals("Basic", basic.getTemplateName());
        assertEquals("MONTHLY", basic.getPlanType());
        assertEquals("MonthlyFee", basic.getFeeStrategy());
        assertEquals(1500.0, basic.getBaseFee());
        assertFalse(basic.isLocker());
        assertFalse(basic.isDietPlan());
        assertEquals(0, basic.getPtSessions());
        assertNotNull(basic.getFeatures());
        assertFalse(basic.getFeatures().isEmpty());
        assertNull(basic.getMemberId());
    }

    @Test
    @DisplayName("Prototype: PremiumMembershipPrototype default configuration is valid")
    void testPremiumMembershipPrototypeDefaults() {
        PremiumMembershipPrototype premium = new PremiumMembershipPrototype();
        assertEquals("Premium", premium.getTemplateName());
        assertEquals("YEARLY", premium.getPlanType());
        assertEquals("YearlyFee", premium.getFeeStrategy());
        assertEquals(14400.0, premium.getBaseFee());
        assertTrue(premium.isLocker());
        assertTrue(premium.isDietPlan());
        assertEquals(5, premium.getPtSessions());
        assertNotNull(premium.getFeatures());
        assertTrue(premium.getFeatures().size() >= 4);
    }

    @Test
    @DisplayName("Prototype: StudentMembershipPrototype default configuration is valid")
    void testStudentMembershipPrototypeDefaults() {
        StudentMembershipPrototype student = new StudentMembershipPrototype();
        assertEquals("Student", student.getTemplateName());
        assertEquals("STUDENT", student.getPlanType());
        assertEquals("StudentFee", student.getFeeStrategy());
        assertEquals(1000.0, student.getBaseFee());
        assertFalse(student.isLocker());
        assertFalse(student.isDietPlan());
        assertEquals(0, student.getPtSessions());
        assertEquals("true", student.getAttributes().get("idRequired"));
    }

    @Test
    @DisplayName("Prototype: CoupleMembershipPrototype default configuration is valid")
    void testCoupleMembershipPrototypeDefaults() {
        CoupleMembershipPrototype couple = new CoupleMembershipPrototype();
        assertEquals("Couple", couple.getTemplateName());
        assertEquals("COUPLE", couple.getPlanType());
        assertEquals("CoupleFee", couple.getFeeStrategy());
        assertEquals(2500.0, couple.getBaseFee());
        assertTrue(couple.isLocker());
        assertTrue(couple.isDietPlan());
        assertEquals(2, couple.getPtSessions());
        assertEquals("2", couple.getAttributes().get("pax"));
    }

    @Test
    @DisplayName("Prototype: Deep cloning ensures mutable collections are strictly isolated")
    void testDeepCloningIndependence() {
        PremiumMembershipPrototype original = new PremiumMembershipPrototype();
        int originalFeatureCount = original.getFeatures().size();
        int originalAttrCount = original.getAttributes().size();

        // Perform clone
        PremiumMembershipPrototype clone = original.clonePrototype();
        assertNotSame(original, clone, "Clone must be a distinct object instance");

        // Mutate clone's collections
        clone.getFeatures().add("Extra VIP Lounge Access");
        clone.getAttributes().put("customKey", "customValue");
        clone.setLocker(false);
        clone.setPtSessions(10);

        // Verify original prototype was NOT mutated
        assertEquals(originalFeatureCount, original.getFeatures().size(),
                "Modifying clone features must not alter master prototype features");
        assertFalse(original.getFeatures().contains("Extra VIP Lounge Access"));

        assertEquals(originalAttrCount, original.getAttributes().size(),
                "Modifying clone attributes must not alter master prototype attributes");
        assertFalse(original.getAttributes().containsKey("customKey"));

        assertTrue(original.isLocker(), "Original prototype locker flag must remain true");
        assertEquals(5, original.getPtSessions(), "Original prototype PT sessions must remain 5");

        // Verify clone received mutations
        assertEquals(originalFeatureCount + 1, clone.getFeatures().size());
        assertTrue(clone.getFeatures().contains("Extra VIP Lounge Access"));
        assertFalse(clone.isLocker());
        assertEquals(10, clone.getPtSessions());
    }

    @Test
    @DisplayName("Prototype: Member-specific fields remain completely independent across clones")
    void testMemberSpecificDataIndependence() {
        MembershipPrototype clone1 = registry.getClonedPrototype("BASIC");
        MembershipPrototype clone2 = registry.getClonedPrototype("BASIC");

        assertNotSame(clone1, clone2);

        // Customize clone 1
        clone1.setMemberId(101L);
        clone1.setTrainerId(2L);
        clone1.setStartDate(LocalDate.of(2026, 11, 1));
        clone1.setLocker(true);

        // Customize clone 2
        clone2.setMemberId(202L);
        clone2.setTrainerId(1L);
        clone2.setStartDate(LocalDate.of(2026, 12, 1));
        clone2.setLocker(false);

        // Verify clone 1 is isolated
        assertEquals(101L, clone1.getMemberId());
        assertEquals(2L, clone1.getTrainerId());
        assertEquals(LocalDate.of(2026, 11, 1), clone1.getStartDate());
        assertTrue(clone1.isLocker());

        // Verify clone 2 is isolated
        assertEquals(202L, clone2.getMemberId());
        assertEquals(1L, clone2.getTrainerId());
        assertEquals(LocalDate.of(2026, 12, 1), clone2.getStartDate());
        assertFalse(clone2.isLocker());

        // Verify registry master prototype was NOT touched
        MembershipPrototype master = registry.getPrototype("BASIC");
        assertNull(master.getMemberId());
        assertNull(master.getTrainerId());
        assertNull(master.getStartDate());
    }

    @Test
    @DisplayName("Prototype: Registry dispenses fresh clones on each request")
    void testRegistryDispensesFreshClones() {
        MembershipPrototype c1 = registry.getClonedPrototype("PREMIUM");
        MembershipPrototype c2 = registry.getClonedPrototype("PREMIUM");

        assertNotNull(c1);
        assertNotNull(c2);
        assertNotSame(c1, c2, "Each call to getClonedPrototype must return an independent clone");

        // Also test lookup by plan alias "YEARLY"
        MembershipPrototype c3 = registry.getClonedPrototype("YEARLY");
        assertNotSame(c1, c3);
        assertEquals("YEARLY", c3.getPlanType());
    }

    @Test
    @DisplayName("Prototype: Registry rejects unknown or blank template keys with descriptive exception")
    void testRegistryRejectsUnknownKeys() {
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class,
                () -> registry.getPrototype("DIAMOND_ELITE"));
        assertTrue(ex1.getMessage().contains("Unknown membership prototype template key: 'DIAMOND_ELITE'"));

        assertThrows(IllegalArgumentException.class, () -> registry.getPrototype(null));
        assertThrows(IllegalArgumentException.class, () -> registry.getPrototype("   "));
        assertThrows(IllegalArgumentException.class, () -> registry.getClonedPrototype("NON_EXISTENT"));
    }

    @Test
    @DisplayName("Prototype: toMembership() uses Membership.Builder and validates constraints")
    void testPrototypeToMembershipBuilder() {
        MembershipPrototype clone = registry.getClonedPrototype("COUPLE");
        clone.setMemberId(77L);
        clone.setStartDate(LocalDate.of(2026, 6, 1));
        clone.setTrainerId(2L);

        Membership membership = clone.toMembership();

        assertNotNull(membership);
        assertEquals(77L, membership.getMemberId());
        assertEquals("COUPLE", membership.getPlanType());
        assertEquals("CoupleFee", membership.getFeeStrategy());
        assertEquals(2500.0, membership.getBaseFee());
        assertTrue(membership.isLocker());
        assertTrue(membership.isDietPlan());
        assertEquals(2, membership.getPtSessions());
        assertEquals(2L, membership.getTrainerId());
        assertEquals(LocalDate.of(2026, 6, 1), membership.getStartDate());
        assertEquals("ACTIVE", membership.getStatus());
        // Derived end date for monthly/couple plan
        assertEquals(LocalDate.of(2026, 7, 1), membership.getEndDate());
    }

    @Test
    @DisplayName("Prototype: applyToForm() properly populates EnrollmentForm DTO")
    void testApplyToForm() {
        MembershipPrototype clone = registry.getClonedPrototype("PREMIUM");
        EnrollmentForm form = new EnrollmentForm();

        clone.applyToForm(form);

        assertEquals("Premium", form.getTemplateType());
        assertEquals("YEARLY", form.getPlanType());
        assertEquals("YearlyFee", form.getStrategyName());
        assertTrue(form.isLocker());
        assertTrue(form.isDietPlan());
        assertEquals(5, form.getPtSessions());
    }

    @Test
    @DisplayName("Prototype: Custom prototype can be registered and retrieved at runtime")
    void testCustomPrototypeRegistration() {
        BasicMembershipPrototype customCorporate = new BasicMembershipPrototype();
        customCorporate.setTemplateName("Corporate");
        customCorporate.setPlanType("MONTHLY");
        customCorporate.setBaseFee(1200.0);
        customCorporate.getFeatures().add("Corporate Wellness Partnership Access");

        registry.registerPrototype("CORPORATE", customCorporate);

        assertTrue(registry.containsPrototype("CORPORATE"));
        MembershipPrototype cloned = registry.getClonedPrototype("CORPORATE");
        assertEquals("Corporate", cloned.getTemplateName());
        assertEquals(1200.0, cloned.getBaseFee());
        assertNotSame(customCorporate, cloned);
    }
}
