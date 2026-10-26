package com.gym.pattern;

import com.gym.model.entity.Membership;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PATTERN TEST: Builder Pattern Validation
 * Verifies that Membership.Builder enforces required constraints and validates input state.
 */
class MembershipBuilderTest {

    @Test
    @DisplayName("Builder: Successfully constructs Membership with all required fields")
    void testBuilderSuccessWithRequiredFields() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        Membership membership = new Membership.Builder(1L, "MONTHLY", startDate).build();

        assertNotNull(membership);
        assertEquals(1L, membership.getMemberId());
        assertEquals("MONTHLY", membership.getPlanType());
        assertEquals(startDate, membership.getStartDate());
        assertEquals(startDate.plusMonths(1), membership.getEndDate());
        assertFalse(membership.isLocker());
        assertFalse(membership.isDietPlan());
        assertEquals(0, membership.getPtSessions());
        assertEquals("ACTIVE", membership.getStatus());
    }

    @Test
    @DisplayName("Builder: Successfully sets optional fields (trainer, locker, dietPlan, ptSessions)")
    void testBuilderSuccessWithOptionalFields() {
        LocalDate startDate = LocalDate.of(2026, 2, 1);
        Membership membership = new Membership.Builder(5L, "YEARLY", startDate)
                .trainer(2L)
                .locker(true)
                .dietPlan(true)
                .ptSessions(5)
                .feeStrategy("YearlyFee")
                .baseFee(14400.0)
                .totalFee(22800.0)
                .build();

        assertNotNull(membership);
        assertEquals(5L, membership.getMemberId());
        assertEquals("YEARLY", membership.getPlanType());
        assertEquals(startDate.plusYears(1), membership.getEndDate());
        assertEquals(2L, membership.getTrainerId());
        assertTrue(membership.isLocker());
        assertTrue(membership.isDietPlan());
        assertEquals(5, membership.getPtSessions());
        assertEquals("YearlyFee", membership.getFeeStrategy());
        assertEquals(14400.0, membership.getBaseFee());
        assertEquals(22800.0, membership.getTotalFee());
    }

    @Test
    @DisplayName("Builder Validation: Throws IllegalStateException when memberId is null")
    void testBuilderValidationNullMemberId() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new Membership.Builder(null, "MONTHLY", LocalDate.now()).build());
        assertTrue(ex.getMessage().contains("memberId is required"));
    }

    @Test
    @DisplayName("Builder Validation: Throws IllegalStateException when memberId is zero or negative")
    void testBuilderValidationInvalidMemberId() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new Membership.Builder(-1L, "MONTHLY", LocalDate.now()).build());
        assertTrue(ex.getMessage().contains("memberId is required"));
    }

    @Test
    @DisplayName("Builder Validation: Throws IllegalStateException when planType is null or blank")
    void testBuilderValidationMissingPlanType() {
        IllegalStateException exNull = assertThrows(IllegalStateException.class, () ->
                new Membership.Builder(1L, null, LocalDate.now()).build());
        assertTrue(exNull.getMessage().contains("planType is required"));

        IllegalStateException exBlank = assertThrows(IllegalStateException.class, () ->
                new Membership.Builder(1L, "   ", LocalDate.now()).build());
        assertTrue(exBlank.getMessage().contains("planType is required"));
    }

    @Test
    @DisplayName("Builder Validation: Throws IllegalStateException when startDate is null")
    void testBuilderValidationNullStartDate() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new Membership.Builder(1L, "MONTHLY", null).build());
        assertTrue(ex.getMessage().contains("startDate is required"));
    }

    @Test
    @DisplayName("Builder Validation: Throws IllegalStateException when ptSessions is negative")
    void testBuilderValidationNegativePtSessions() {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                new Membership.Builder(1L, "MONTHLY", LocalDate.now())
                        .ptSessions(-3)
                        .build());
        assertTrue(ex.getMessage().contains("ptSessions cannot be negative"));
    }
}
