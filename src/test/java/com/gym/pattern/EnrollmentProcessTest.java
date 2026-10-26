package com.gym.pattern;

import com.gym.dao.MemberDAO;
import com.gym.dao.MembershipDAO;
import com.gym.dao.PaymentDAO;
import com.gym.dao.PlanHistoryDAO;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.pattern.mediator.GymMediator;
import com.gym.pattern.strategy.FeeStrategyFactory;
import com.gym.pattern.strategy.MonthlyFee;
import com.gym.pattern.strategy.YearlyFee;
import com.gym.pattern.templatemethod.EnrollmentContext;
import com.gym.pattern.templatemethod.NewEnrollment;
import com.gym.pattern.templatemethod.RenewalEnrollment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PATTERN TEST: Template Method Step Order Verification
 * Verifies that EnrollmentProcess.final run() strictly guarantees the execution order:
 * 1. verifyMember -> 2. collectFee -> 3. activateMembership -> 4. sendWelcome
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentProcessTest {

    @Mock
    private MemberDAO memberDAO;
    @Mock
    private MembershipDAO membershipDAO;
    @Mock
    private PaymentDAO paymentDAO;
    @Mock
    private PlanHistoryDAO planHistoryDAO;
    @Mock
    private GymMediator gymMediator;

    private FeeStrategyFactory feeStrategyFactory;
    private NewEnrollment newEnrollment;
    private RenewalEnrollment renewalEnrollment;

    @BeforeEach
    void setUp() {
        feeStrategyFactory = new FeeStrategyFactory(List.of(new MonthlyFee(), new YearlyFee()));
        newEnrollment = new NewEnrollment(memberDAO, membershipDAO, paymentDAO, feeStrategyFactory, gymMediator, planHistoryDAO);
        renewalEnrollment = new RenewalEnrollment(memberDAO, membershipDAO, paymentDAO, feeStrategyFactory, gymMediator, planHistoryDAO);
    }

    @Test
    @DisplayName("Template Method: Guarantees invariant step order in NewEnrollment")
    void testNewEnrollmentStepOrder() {
        Member member = new Member(10L, "Alice Brown", "alice@example.com", "+1-555-0199", "ACTIVE", LocalDate.now());
        Membership membership = new Membership.Builder(10L, "MONTHLY", LocalDate.now())
                .locker(true)
                .build();

        when(memberDAO.findById(10L)).thenReturn(Optional.of(member));
        when(membershipDAO.findActiveByMemberId(10L)).thenReturn(Optional.empty());
        when(membershipDAO.create(any(Membership.class))).thenReturn(100L);

        EnrollmentContext context = new EnrollmentContext(member, membership, "NEW");
        EnrollmentContext result = newEnrollment.run(context);

        // Verify invariant execution step order
        List<String> steps = result.getStepExecutionOrder();
        assertEquals(4, steps.size());
        assertEquals("verifyMember", steps.get(0));
        assertEquals("collectFee", steps.get(1));
        assertEquals("activateMembership", steps.get(2));
        assertEquals("sendWelcome", steps.get(3));

        assertTrue(result.isSuccessful());
        assertNotNull(result.getWelcomeMessage());
        assertTrue(result.getWelcomeMessage().contains("Welcome to the Gym Family"));

        // Verify mediator notification dispatched on activation
        verify(gymMediator).notify(eq(newEnrollment), eq("MEMBER_ENROLLED"), any(Membership.class));
        verify(paymentDAO).create(any());
        verify(planHistoryDAO).saveSnapshot(eq(10L), eq("ENROLLMENT"), any());
    }

    @Test
    @DisplayName("Template Method: Guarantees invariant step order and loyalty discount in RenewalEnrollment")
    void testRenewalEnrollmentStepOrderAndDiscount() {
        Member member = new Member(20L, "Bob White", "bob@example.com", "+1-555-0188", "ACTIVE", LocalDate.now());
        Membership activeMembership = new Membership.Builder(20L, "MONTHLY", LocalDate.now().minusMonths(1))
                .endDate(LocalDate.now())
                .build();
        activeMembership.setId(200L);

        when(memberDAO.findById(20L)).thenReturn(Optional.of(member));
        when(membershipDAO.findActiveByMemberId(20L)).thenReturn(Optional.of(activeMembership));

        Membership renewalCandidate = new Membership.Builder(20L, "MONTHLY", LocalDate.now()).build();
        EnrollmentContext context = new EnrollmentContext(member, renewalCandidate, "RENEWAL");

        EnrollmentContext result = renewalEnrollment.run(context);

        // Verify invariant execution step order
        List<String> steps = result.getStepExecutionOrder();
        assertEquals(4, steps.size());
        assertEquals("verifyMember", steps.get(0));
        assertEquals("collectFee", steps.get(1));
        assertEquals("activateMembership", steps.get(2));
        assertEquals("sendWelcome", steps.get(3));

        assertTrue(result.isSuccessful());
        // Verify 5% Loyalty Renewal Discount (₹1,500 standard * 0.95 = ₹1,425.00)
        assertEquals(1425.00, result.getMembership().getTotalFee());
        assertTrue(result.getWelcomeMessage().contains("Loyalty Discount"));

        verify(gymMediator).notify(eq(renewalEnrollment), eq("MEMBER_RENEWED"), any(Membership.class));
        verify(planHistoryDAO).saveSnapshot(eq(20L), eq("RENEWAL"), any());
    }

    @Test
    @DisplayName("Template Method: Rejects new enrollment if member already has active membership")
    void testNewEnrollmentRejectionIfAlreadyActive() {
        Member member = new Member(30L, "Charlie", "charlie@example.com", "+1-555-0177", "ACTIVE", LocalDate.now());
        Membership existingActive = new Membership.Builder(30L, "YEARLY", LocalDate.now()).build();

        when(memberDAO.findById(30L)).thenReturn(Optional.of(member));
        when(membershipDAO.findActiveByMemberId(30L)).thenReturn(Optional.of(existingActive));

        EnrollmentContext context = new EnrollmentContext(member, new Membership.Builder(30L, "MONTHLY", LocalDate.now()).build(), "NEW");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> newEnrollment.run(context));
        assertTrue(ex.getMessage().contains("already holds an ACTIVE membership"));
    }
}
