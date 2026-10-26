package com.gym.pattern;

import com.gym.model.entity.Membership;
import com.gym.pattern.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PATTERN TEST: Strategy Pattern Fee Calculation
 * Verifies each concrete FeeStrategy (MonthlyFee, YearlyFee, StudentFee, CoupleFee)
 * and verifies map-based resolution via FeeStrategyFactory without if/else branching.
 */
class FeeStrategyTest {

    private MonthlyFee monthlyFee;
    private YearlyFee yearlyFee;
    private StudentFee studentFee;
    private CoupleFee coupleFee;
    private FeeStrategyFactory factory;

    @BeforeEach
    void setUp() {
        monthlyFee = new MonthlyFee();
        yearlyFee = new YearlyFee();
        studentFee = new StudentFee();
        coupleFee = new CoupleFee();
        factory = new FeeStrategyFactory(List.of(monthlyFee, yearlyFee, studentFee, coupleFee));
    }

    @Test
    @DisplayName("Strategy: MonthlyFee calculation with base and optional add-ons")
    void testMonthlyFeeStrategy() {
        // Base monthly fee (₹1,500)
        Membership base = new Membership.Builder(1L, "MONTHLY", LocalDate.now()).build();
        assertEquals(1500.0, monthlyFee.calculateFee(base));

        // Monthly fee with locker (₹1,500 + ₹300 = ₹1,800)
        Membership withLocker = new Membership.Builder(1L, "MONTHLY", LocalDate.now())
                .locker(true).build();
        assertEquals(1800.0, monthlyFee.calculateFee(withLocker));

        // Monthly fee with all add-ons (₹1,500 + ₹300 locker + ₹500 diet + 3 * ₹500 PT = ₹3,800)
        Membership fullyLoaded = new Membership.Builder(1L, "MONTHLY", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(3)
                .build();
        assertEquals(3800.0, monthlyFee.calculateFee(fullyLoaded));
    }

    @Test
    @DisplayName("Strategy: YearlyFee calculation with commitment discounts")
    void testYearlyFeeStrategy() {
        // Base yearly fee (₹14,400)
        Membership base = new Membership.Builder(1L, "YEARLY", LocalDate.now()).build();
        assertEquals(14400.0, yearlyFee.calculateFee(base));

        // Yearly fee with all add-ons (₹14,400 base + ₹2,400 locker + ₹4,000 diet + 4 * ₹400 PT = ₹22,400)
        Membership fullYearly = new Membership.Builder(1L, "YEARLY", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(4)
                .build();
        assertEquals(22400.0, yearlyFee.calculateFee(fullYearly));
    }

    @Test
    @DisplayName("Strategy: StudentFee calculation with student pricing")
    void testStudentFeeStrategy() {
        // Base student fee (₹1,000)
        Membership base = new Membership.Builder(1L, "STUDENT", LocalDate.now()).build();
        assertEquals(1000.0, studentFee.calculateFee(base));

        // Student fee with locker (₹1,000 + ₹200 = ₹1,200)
        Membership withLocker = new Membership.Builder(1L, "STUDENT", LocalDate.now())
                .locker(true).build();
        assertEquals(1200.0, studentFee.calculateFee(withLocker));

        // Student fee with all add-ons (₹1,000 base + ₹200 locker + ₹300 diet + 2 * ₹350 PT = ₹2,200)
        Membership fullStudent = new Membership.Builder(1L, "STUDENT", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(2)
                .build();
        assertEquals(2200.0, studentFee.calculateFee(fullStudent));
    }

    @Test
    @DisplayName("Strategy: CoupleFee calculation for partner memberships")
    void testCoupleFeeStrategy() {
        // Base couple fee (₹2,500)
        Membership base = new Membership.Builder(1L, "COUPLE", LocalDate.now()).build();
        assertEquals(2500.0, coupleFee.calculateFee(base));

        // Couple fee with all add-ons (₹2,500 base + ₹500 locker + ₹800 diet + 2 * ₹800 PT = ₹5,400)
        Membership fullCouple = new Membership.Builder(1L, "COUPLE", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(2)
                .build();
        assertEquals(5400.0, coupleFee.calculateFee(fullCouple));
    }

    @Test
    @DisplayName("Strategy Factory: Map-based lookup without if/else branching")
    void testFeeStrategyFactoryMapLookup() {
        assertSame(monthlyFee, factory.getStrategy("MonthlyFee"));
        assertSame(monthlyFee, factory.getStrategy("MONTHLY"));
        assertSame(yearlyFee, factory.getStrategy("YearlyFee"));
        assertSame(yearlyFee, factory.getStrategy("YEARLY"));
        assertSame(studentFee, factory.getStrategy("StudentFee"));
        assertSame(studentFee, factory.getStrategy("STUDENT"));
        assertSame(coupleFee, factory.getStrategy("CoupleFee"));
        assertSame(coupleFee, factory.getStrategy("COUPLE"));
    }

    @Test
    @DisplayName("Strategy Factory: Throws IllegalArgumentException for unknown plan")
    void testFeeStrategyFactoryUnknownKey() {
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy("VIP_GOLD_DIAMOND"));
        assertThrows(IllegalArgumentException.class, () -> factory.getStrategy(null));
    }
}
