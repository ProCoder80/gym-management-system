package com.gym.pattern;

import com.gym.dao.MembershipDAO;
import com.gym.dao.PlanHistoryDAO;
import com.gym.model.entity.Membership;
import com.gym.model.entity.PlanHistoryEntry;
import com.gym.pattern.memento.MemberPlan;
import com.gym.pattern.strategy.FeeStrategyFactory;
import com.gym.pattern.strategy.MonthlyFee;
import com.gym.pattern.strategy.YearlyFee;
import com.gym.service.PlanManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * PATTERN TEST: Memento Pattern State Preservation & Undo Rollback
 * Verifies Originator (MemberPlan) snapshot creation, restoration, and Caretaker (PlanManagementService)
 * undo operations via the SQLite plan_history stack.
 */
@ExtendWith(MockitoExtension.class)
class MemberPlanMementoTest {

    @Mock
    private MembershipDAO membershipDAO;
    @Mock
    private PlanHistoryDAO planHistoryDAO;

    private FeeStrategyFactory feeStrategyFactory;
    private PlanManagementService planManagementService;

    @BeforeEach
    void setUp() {
        feeStrategyFactory = new FeeStrategyFactory(List.of(new MonthlyFee(), new YearlyFee()));
        planManagementService = new PlanManagementService(membershipDAO, planHistoryDAO, feeStrategyFactory);
    }

    @Test
    @DisplayName("Memento: Originator captures internal state into immutable Memento")
    void testOriginatorSaveToMemento() {
        MemberPlan plan = new MemberPlan(1L, "MONTHLY", 1800.0, true, false, 0, "ACTIVE");

        MemberPlan.Memento snapshot = plan.saveToMemento();

        assertNotNull(snapshot);
        assertEquals("MONTHLY", snapshot.getPlanType());
        assertEquals(1800.0, snapshot.getTotalFee());
        assertTrue(snapshot.isLocker());
        assertFalse(snapshot.isDietPlan());
        assertEquals(0, snapshot.getPtSessions());
        assertEquals("ACTIVE", snapshot.getStatus());
        assertNotNull(snapshot.getTimestamp());
    }

    @Test
    @DisplayName("Memento: Originator successfully restores state from Memento")
    void testOriginatorRestoreFromMemento() {
        MemberPlan plan = new MemberPlan(1L, "MONTHLY", 1800.0, true, false, 0, "ACTIVE");

        // Take snapshot of initial state
        MemberPlan.Memento initialMemento = plan.saveToMemento();

        // Mutate originator state via upgrade
        plan.upgrade("YEARLY", 22400.0, true, true, 4);
        assertEquals("YEARLY", plan.getPlanType());
        assertEquals(22400.0, plan.getTotalFee());
        assertTrue(plan.isDietPlan());
        assertEquals(4, plan.getPtSessions());

        // Restore back to original memento
        plan.restoreFromMemento(initialMemento);

        assertEquals("MONTHLY", plan.getPlanType());
        assertEquals(1800.0, plan.getTotalFee());
        assertTrue(plan.isLocker());
        assertFalse(plan.isDietPlan());
        assertEquals(0, plan.getPtSessions());
        assertEquals("ACTIVE", plan.getStatus());
    }

    @Test
    @DisplayName("Memento Undo: Caretaker restores previous state from history stack and updates DB")
    void testPlanManagementServiceUndoOperation() {
        Long memberId = 1L;

        Membership currentMembership = new Membership.Builder(memberId, "YEARLY", LocalDate.now())
                .locker(true)
                .dietPlan(true)
                .ptSessions(4)
                .baseFee(14400.0)
                .totalFee(22400.0)
                .build();
        currentMembership.setId(10L);

        // Previous historical snapshot was MONTHLY, ₹1,800, locker=true, dietPlan=false, ptSessions=0
        PlanHistoryEntry previousSnapshot = new PlanHistoryEntry(
                1L,
                memberId,
                "ENROLLMENT",
                "MONTHLY",
                1800.0,
                true,
                false,
                0,
                "ACTIVE",
                "{\"planType\":\"MONTHLY\"}",
                LocalDateTime.now().minusDays(5)
        );

        when(membershipDAO.findActiveByMemberId(memberId)).thenReturn(Optional.of(currentMembership));
        when(planHistoryDAO.findPreviousSnapshot(memberId)).thenReturn(Optional.of(previousSnapshot));
        when(membershipDAO.update(any(Membership.class))).thenReturn(true);

        // Execute Undo
        boolean success = planManagementService.undoPlanChange(memberId);

        assertTrue(success);

        // Verify currentMembership was restored to the previous Memento state
        assertEquals("MONTHLY", currentMembership.getPlanType());
        assertEquals(1800.0, currentMembership.getTotalFee());
        assertTrue(currentMembership.isLocker());
        assertFalse(currentMembership.isDietPlan());
        assertEquals(0, currentMembership.getPtSessions());

        // Verify database update occurred and UNDO snapshot was recorded
        verify(membershipDAO).update(currentMembership);
        verify(planHistoryDAO).saveSnapshot(eq(memberId), eq("UNDO"), any(MemberPlan.Memento.class));
    }

    @Test
    @DisplayName("Memento Undo: Throws exception when no previous snapshot exists in history")
    void testUndoWithNoPreviousHistoryThrowsException() {
        Long memberId = 2L;
        Membership currentMembership = new Membership.Builder(memberId, "MONTHLY", LocalDate.now()).build();

        when(membershipDAO.findActiveByMemberId(memberId)).thenReturn(Optional.of(currentMembership));
        when(planHistoryDAO.findPreviousSnapshot(memberId)).thenReturn(Optional.empty());

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                planManagementService.undoPlanChange(memberId));
        assertTrue(ex.getMessage().contains("No previous plan state found in history stack to undo"));
    }
}
