package com.gym.pattern;

import com.gym.dao.MemberDAO;
import com.gym.dao.MembershipDAO;
import com.gym.dao.PaymentDAO;
import com.gym.dao.PlanHistoryDAO;
import com.gym.model.dto.EnrollmentForm;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.model.prototype.MembershipPrototype;
import com.gym.model.prototype.MembershipPrototypeRegistry;
import com.gym.pattern.mediator.GymMediator;
import com.gym.pattern.strategy.CoupleFee;
import com.gym.pattern.strategy.FeeStrategyFactory;
import com.gym.pattern.strategy.MonthlyFee;
import com.gym.pattern.strategy.StudentFee;
import com.gym.pattern.strategy.YearlyFee;
import com.gym.pattern.templatemethod.EnrollmentContext;
import com.gym.pattern.templatemethod.NewEnrollment;
import com.gym.pattern.templatemethod.RenewalEnrollment;
import com.gym.service.EnrollmentService;
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
import static org.mockito.Mockito.when;

/**
 * INTEGRATION TEST: Verifies seamless interaction between:
 * - Prototype Pattern (MembershipPrototypeRegistry)
 * - Builder Pattern (Membership.Builder)
 * - Strategy Pattern (FeeStrategyFactory)
 * - Template Method Pattern (EnrollmentProcess / NewEnrollment)
 * - Mediator Pattern (GymMediator)
 */
@ExtendWith(MockitoExtension.class)
class EnrollmentServicePrototypeIntegrationTest {

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

    private MembershipPrototypeRegistry prototypeRegistry;
    private EnrollmentService enrollmentService;

    @BeforeEach
    void setUp() {
        FeeStrategyFactory feeStrategyFactory = new FeeStrategyFactory(List.of(
                new MonthlyFee(), new YearlyFee(), new StudentFee(), new CoupleFee()
        ));
        prototypeRegistry = new MembershipPrototypeRegistry();

        NewEnrollment newEnrollment = new NewEnrollment(
                memberDAO, membershipDAO, paymentDAO, feeStrategyFactory, gymMediator, planHistoryDAO
        );
        RenewalEnrollment renewalEnrollment = new RenewalEnrollment(
                memberDAO, membershipDAO, paymentDAO, feeStrategyFactory, gymMediator, planHistoryDAO
        );

        enrollmentService = new EnrollmentService(
                newEnrollment, renewalEnrollment, memberDAO, prototypeRegistry
        );
    }

    @Test
    @DisplayName("Integration: Prepopulates from Premium Prototype and completes full enrollment")
    void testEnrollmentWithPremiumPrototype() {
        Member member = new Member(50L, "Dev Patel", "dev@example.com", "+91-99887-11223", "ACTIVE", LocalDate.now());
        when(memberDAO.findById(50L)).thenReturn(Optional.of(member));
        when(membershipDAO.findActiveByMemberId(50L)).thenReturn(Optional.empty());
        when(membershipDAO.create(any(Membership.class))).thenReturn(500L);

        // Client chooses PREMIUM template, customizes PT sessions to 8 instead of default 5
        EnrollmentForm form = new EnrollmentForm();
        form.setMemberId(50L);
        form.setTemplateType("PREMIUM");
        form.setPlanType("YEARLY");
        form.setStrategyName("YearlyFee");
        form.setLocker(true);
        form.setDietPlan(true);
        form.setPtSessions(8);
        form.setEnrollmentType("NEW");

        EnrollmentContext context = enrollmentService.executeEnrollment(form);

        assertTrue(context.isSuccessful());
        assertEquals("Premium", context.getTemplateName());
        assertNotNull(context.getMembership());
        assertEquals("YEARLY", context.getMembership().getPlanType());
        assertTrue(context.getMembership().isLocker());
        assertTrue(context.getMembership().isDietPlan());
        assertEquals(8, context.getMembership().getPtSessions());

        // Strategy calculation: Base 14400 + Locker 2400 + Diet 4000 + 8 * 400 (3200) = 24000
        assertEquals(24000.0, context.getMembership().getTotalFee());

        // Template message confirmation mentions template
        assertTrue(context.getWelcomeMessage().contains("Template: Premium"));

        // Verify master prototype in registry remained untouched with default 5 sessions
        MembershipPrototype master = prototypeRegistry.getPrototype("PREMIUM");
        assertEquals(5, master.getPtSessions(), "Master prototype PT sessions must stay at default 5");
    }
}
