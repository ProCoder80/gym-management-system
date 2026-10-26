package com.gym.service;

import com.gym.dao.MembershipDAO;
import com.gym.dao.PlanHistoryDAO;
import com.gym.model.entity.Membership;
import com.gym.model.entity.PlanHistoryEntry;
import com.gym.pattern.memento.MemberPlan;
import com.gym.pattern.strategy.FeeStrategy;
import com.gym.pattern.strategy.FeeStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * PATTERN: Memento Pattern Caretaker Service
 * 
 * Coordinates:
 * - Originator (MemberPlan) state captures and restores.
 * - History Stack persistence in the SQLite plan_history table.
 * - Supports upgrade, downgrade, and undo capabilities.
 */
@Service
public class PlanManagementService {

    private static final Logger logger = LoggerFactory.getLogger(PlanManagementService.class);

    private final MembershipDAO membershipDAO;
    private final PlanHistoryDAO planHistoryDAO;
    private final FeeStrategyFactory feeStrategyFactory;

    public PlanManagementService(MembershipDAO membershipDAO,
                                 PlanHistoryDAO planHistoryDAO,
                                 FeeStrategyFactory feeStrategyFactory) {
        this.membershipDAO = membershipDAO;
        this.planHistoryDAO = planHistoryDAO;
        this.feeStrategyFactory = feeStrategyFactory;
    }

    public Optional<Membership> getActiveMembership(Long memberId) {
        return membershipDAO.findActiveByMemberId(memberId);
    }

    public List<PlanHistoryEntry> getPlanHistory(Long memberId) {
        return planHistoryDAO.findByMemberId(memberId);
    }

    /**
     * UPGRADE PLAN:
     * 1. Capture current state into Memento and push to history stack.
     * 2. Recalculate fee with Strategy pattern.
     * 3. Apply upgrade to Originator (MemberPlan).
     * 4. Persist updated Membership in database.
     */
    public boolean upgradePlan(Long memberId, String newPlanType, boolean locker, boolean dietPlan, int ptSessions) {
        Optional<Membership> activeOpt = membershipDAO.findActiveByMemberId(memberId);
        if (activeOpt.isEmpty()) {
            throw new IllegalStateException("No active membership found to upgrade for Member ID: " + memberId);
        }

        Membership current = activeOpt.get();

        // Step 1: Initialize Originator with CURRENT state
        MemberPlan planOriginator = new MemberPlan(
                memberId,
                current.getPlanType(),
                current.getTotalFee(),
                current.isLocker(),
                current.isDietPlan(),
                current.getPtSessions(),
                current.getStatus()
        );

        // Step 2: Push current state snapshot (Memento) to history BEFORE modifying
        MemberPlan.Memento snapshotBeforeChange = planOriginator.saveToMemento();
        planHistoryDAO.saveSnapshot(memberId, "UPGRADE_FROM", snapshotBeforeChange);

        // Step 3: Calculate new fee using FeeStrategy (via factory lookup)
        Membership dummyForCalc = new Membership.Builder(memberId, newPlanType, LocalDate.now())
                .locker(locker)
                .dietPlan(dietPlan)
                .ptSessions(ptSessions)
                .build();
        FeeStrategy strategy = feeStrategyFactory.getStrategy(newPlanType);
        double newTotalFee = strategy.calculateFee(dummyForCalc);

        // Step 4: Mutate Originator
        planOriginator.upgrade(newPlanType, newTotalFee, locker, dietPlan, ptSessions);

        // Step 5: Push updated state to history
        MemberPlan.Memento upgradedMemento = planOriginator.saveToMemento();
        planHistoryDAO.saveSnapshot(memberId, "UPGRADE", upgradedMemento);

        // Step 6: Update database record
        current.setPlanType(newPlanType);
        current.setFeeStrategy(strategy.getStrategyName());
        current.setBaseFee(strategy.getBaseFee());
        current.setTotalFee(newTotalFee);
        current.setLocker(locker);
        current.setDietPlan(dietPlan);
        current.setPtSessions(ptSessions);

        logger.info("Successfully upgraded plan for Member ID: {} to {}", memberId, newPlanType);
        return membershipDAO.update(current);
    }

    /**
     * DOWNGRADE PLAN:
     * 1. Capture current state into Memento and push to history stack.
     * 2. Recalculate fee with Strategy pattern.
     * 3. Apply downgrade to Originator (MemberPlan).
     * 4. Persist updated Membership in database.
     */
    public boolean downgradePlan(Long memberId, String newPlanType, boolean locker, boolean dietPlan, int ptSessions) {
        Optional<Membership> activeOpt = membershipDAO.findActiveByMemberId(memberId);
        if (activeOpt.isEmpty()) {
            throw new IllegalStateException("No active membership found to downgrade for Member ID: " + memberId);
        }

        Membership current = activeOpt.get();

        // Step 1: Initialize Originator
        MemberPlan planOriginator = new MemberPlan(
                memberId,
                current.getPlanType(),
                current.getTotalFee(),
                current.isLocker(),
                current.isDietPlan(),
                current.getPtSessions(),
                current.getStatus()
        );

        // Step 2: Push current state snapshot to history
        MemberPlan.Memento snapshotBeforeChange = planOriginator.saveToMemento();
        planHistoryDAO.saveSnapshot(memberId, "DOWNGRADE_FROM", snapshotBeforeChange);

        // Step 3: Calculate new fee using Strategy
        Membership dummyForCalc = new Membership.Builder(memberId, newPlanType, LocalDate.now())
                .locker(locker)
                .dietPlan(dietPlan)
                .ptSessions(ptSessions)
                .build();
        FeeStrategy strategy = feeStrategyFactory.getStrategy(newPlanType);
        double newTotalFee = strategy.calculateFee(dummyForCalc);

        // Step 4: Mutate Originator
        planOriginator.downgrade(newPlanType, newTotalFee, locker, dietPlan, ptSessions);

        // Step 5: Push downgraded state to history
        MemberPlan.Memento downgradedMemento = planOriginator.saveToMemento();
        planHistoryDAO.saveSnapshot(memberId, "DOWNGRADE", downgradedMemento);

        // Step 6: Update database record
        current.setPlanType(newPlanType);
        current.setFeeStrategy(strategy.getStrategyName());
        current.setBaseFee(strategy.getBaseFee());
        current.setTotalFee(newTotalFee);
        current.setLocker(locker);
        current.setDietPlan(dietPlan);
        current.setPtSessions(ptSessions);

        logger.info("Successfully downgraded plan for Member ID: {} to {}", memberId, newPlanType);
        return membershipDAO.update(current);
    }

    /**
     * UNDO LAST PLAN CHANGE (MEMENTO RESTORATION):
     * 1. Retrieve the previous Memento snapshot from the SQLite plan_history stack.
     * 2. Originator restores its state via restoreFromMemento().
     * 3. Synced back to the memberships table.
     * 4. Logs UNDO event in history stack.
     */
    public boolean undoPlanChange(Long memberId) {
        Optional<Membership> activeOpt = membershipDAO.findActiveByMemberId(memberId);
        if (activeOpt.isEmpty()) {
            throw new IllegalStateException("No active membership found to undo for Member ID: " + memberId);
        }

        // Fetch previous snapshot from history
        Optional<PlanHistoryEntry> previousOpt = planHistoryDAO.findPreviousSnapshot(memberId);
        if (previousOpt.isEmpty()) {
            throw new IllegalStateException("No previous plan state found in history stack to undo.");
        }

        PlanHistoryEntry prev = previousOpt.get();
        MemberPlan.Memento restoredMemento = new MemberPlan.Memento(
                prev.getPlanType(),
                prev.getTotalFee(),
                prev.isLocker(),
                prev.isDietPlan(),
                prev.getPtSessions(),
                prev.getStatus(),
                LocalDateTime.now()
        );

        // Originator restores internal state
        MemberPlan originator = new MemberPlan();
        originator.restoreFromMemento(restoredMemento);

        // Update database membership entity
        Membership current = activeOpt.get();
        current.setPlanType(originator.getPlanType());
        current.setTotalFee(originator.getTotalFee());
        current.setLocker(originator.isLocker());
        current.setDietPlan(originator.isDietPlan());
        current.setPtSessions(originator.getPtSessions());
        current.setStatus(originator.getStatus());

        // Update strategy name based on restored plan type
        try {
            FeeStrategy strat = feeStrategyFactory.getStrategy(originator.getPlanType());
            current.setFeeStrategy(strat.getStrategyName());
            current.setBaseFee(strat.getBaseFee());
        } catch (Exception ignored) {
        }

        boolean updated = membershipDAO.update(current);

        // Record the UNDO action in plan_history
        planHistoryDAO.saveSnapshot(memberId, "UNDO", restoredMemento);

        logger.info("Successfully rolled back Member ID: {} plan to restored Memento state: {}", memberId, restoredMemento.getPlanType());
        return updated;
    }
}
