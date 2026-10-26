package com.gym.pattern.templatemethod;

import com.gym.dao.MemberDAO;
import com.gym.dao.MembershipDAO;
import com.gym.dao.PaymentDAO;
import com.gym.pattern.mediator.GymMediator;
import com.gym.pattern.strategy.FeeStrategy;
import com.gym.pattern.strategy.FeeStrategyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * PATTERN: Template Method Pattern
 * 
 * Intent:
 * Defines the skeleton of an algorithm in an operation, deferring some steps to subclasses.
 * Template Method lets subclasses redefine certain steps of an algorithm without changing
 * the algorithm's structure.
 * 
 * Structure:
 * - final run(EnrollmentContext): Defines the exact, invariant workflow:
 *       1. verifyMember(context)
 *       2. collectFee(context)
 *       3. activateMembership(context)
 *       4. sendWelcome(context)
 * - Subclasses (NewEnrollment, RenewalEnrollment) override only the varying steps,
 *   such as verification constraints, plan activation mechanics, and welcome messaging.
 */
public abstract class EnrollmentProcess {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final MemberDAO memberDAO;
    protected final MembershipDAO membershipDAO;
    protected final PaymentDAO paymentDAO;
    protected final FeeStrategyFactory feeStrategyFactory;
    protected final GymMediator gymMediator;

    public EnrollmentProcess(MemberDAO memberDAO,
                             MembershipDAO membershipDAO,
                             PaymentDAO paymentDAO,
                             FeeStrategyFactory feeStrategyFactory,
                             GymMediator gymMediator) {
        this.memberDAO = memberDAO;
        this.membershipDAO = membershipDAO;
        this.paymentDAO = paymentDAO;
        this.feeStrategyFactory = feeStrategyFactory;
        this.gymMediator = gymMediator;
    }

    /**
     * TEMPLATE METHOD: Declared final so clients/subclasses cannot alter
     * the guaranteed order of steps.
     * 
     * Invariant Step Sequence:
     * 1. verifyMember
     * 2. collectFee
     * 3. activateMembership
     * 4. sendWelcome
     */
    public final EnrollmentContext run(EnrollmentContext context) {
        logger.info("Executing Template Method EnrollmentProcess for memberId: {}", 
                context.getMember() != null ? context.getMember().getId() : "null");

        // Step 1: Verify member qualification and eligibility
        context.recordStep("verifyMember");
        verifyMember(context);

        // Step 2: Calculate and collect subscription fee
        context.recordStep("collectFee");
        collectFee(context);

        // Step 3: Activate membership state in database & coordinate via Mediator
        context.recordStep("activateMembership");
        activateMembership(context);

        // Step 4: Dispatch customized welcome and onboarding notification
        context.recordStep("sendWelcome");
        sendWelcome(context);

        context.setSuccessful(true);
        context.setStatusMessage("Enrollment process completed successfully.");
        return context;
    }

    // =========================================================================
    // Abstract & Hook Methods to be overridden by subclasses
    // =========================================================================

    /**
     * Step 1: Subclass-specific member verification.
     * New enrollment verifies member has no active membership.
     * Renewal verifies member exists and extends previous coverage.
     */
    protected abstract void verifyMember(EnrollmentContext context);

    /**
     * Step 2: Collect fee using Strategy pattern lookup.
     * Common fee calculation and payment recording logic can be shared or specialized.
     */
    protected void collectFee(EnrollmentContext context) {
        FeeStrategy strategy = feeStrategyFactory.getStrategy(context.getMembership().getPlanType());
        double totalFee = strategy.calculateFee(context.getMembership());
        
        context.getMembership().setFeeStrategy(strategy.getStrategyName());
        context.getMembership().setBaseFee(strategy.getBaseFee());
        context.getMembership().setTotalFee(totalFee);

        // Record payment transaction
        String receiptId = "REC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        com.gym.model.entity.Payment payment = new com.gym.model.entity.Payment(
                null,
                context.getMember().getId(),
                null, // will be linked after membership is inserted
                totalFee,
                strategy.getStrategyName(),
                LocalDateTime.now(),
                context.getPaymentMethod(),
                receiptId,
                "COMPLETED"
        );
        context.setPayment(payment);
    }

    /**
     * Step 3: Subclass-specific membership activation and persistence.
     */
    protected abstract void activateMembership(EnrollmentContext context);

    /**
     * Step 4: Subclass-specific welcome notification generation.
     */
    protected abstract void sendWelcome(EnrollmentContext context);
}
