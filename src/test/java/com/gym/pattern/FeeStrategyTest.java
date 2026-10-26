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
        // Base monthly fee ($50)
        Membership base = new Membership.Builder(1L, "MONTHLY", LocalDate.now()).build();
        assertEquals(50.0, monthlyFee.calculateFee(base));

        // Monthly fee with locker ($50 + $15 = $65)
        Membership withLocker = new Membership.Builder(1L, "MONTHLY", LocalDate.now())
                .locker(true).build();
        assertEquals(65.0, monthlyFee.calculateFee(withLocker));

        // Monthly fee with all add-ons ($50 + $15 locker + $25 diet + 3 * $30 PT = $180)
        Membership fullyLoaded = new Membership.Builder(1L, "MONTHLY", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(3)
                .build();
        assertEquals(180.0, monthlyFee.calculateFee(fullyLoaded));
    }

    @Test
    @DisplayName("Strategy: YearlyFee calculation with commitment discounts")
    void testYearlyFeeStrategy() {
        // Base yearly fee ($480)
        Membership base = new Membership.Builder(1L, "YEARLY", LocalDate.now()).build();
        assertEquals(480.0, yearlyFee.calculateFee(base));

        // Yearly fee with all add-ons ($480 base + $120 locker + $200 diet + 4 * $25 PT = $900)
        Membership fullYearly = new Membership.Builder(1L, "YEARLY", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(4)
                .build();
        assertEquals(900.0, yearlyFee.calculateFee(fullYearly));
    }

    @Test
    @DisplayName("Strategy: StudentFee calculation with student pricing")
    void testStudentFeeStrategy() {
        // Base student fee ($35)
        Membership base = new Membership.Builder(1L, "STUDENT", LocalDate.now()).build();
        assertEquals(35.0, studentFee.calculateFee(base));

        // Student fee with locker ($35 + $10 = $45)
        Membership withLocker = new Membership.Builder(1L, "STUDENT", LocalDate.now())
                .locker(true).build();
        assertEquals(45.0, studentFee.calculateFee(withLocker));

        // Student fee with all add-ons ($35 base + $10 locker + $15 diet + 2 * $20 PT = $100)
        Membership fullStudent = new Membership.Builder(1L, "STUDENT", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(2)
                .build();
        assertEquals(100.0, studentFee.calculateFee(fullStudent));
    }

    @Test
    @DisplayName("Strategy: CoupleFee calculation for partner memberships")
    void testCoupleFeeStrategy() {
        // Base couple fee ($80)
        Membership base = new Membership.Builder(1L, "COUPLE", LocalDate.now()).build();
        assertEquals(80.0, coupleFee.calculateFee(base));

        // Couple fee with all add-ons ($80 base + $25 locker + $40 diet + 2 * $45 PT = $235)
        Membership fullCouple = new Membership.Builder(1L, "COUPLE", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(2)
                .build();
        assertEquals(235.0, coupleFee.calculateFee(fullCouple));
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
