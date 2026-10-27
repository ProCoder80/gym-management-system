package com.gym.pattern.templatemethod;

import com.gym.dao.MemberDAO;
import com.gym.dao.MembershipDAO;
import com.gym.dao.PaymentDAO;
import com.gym.dao.PlanHistoryDAO;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.pattern.mediator.GymMediator;
import com.gym.pattern.memento.MemberPlan;
import com.gym.pattern.strategy.FeeStrategyFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

/**
 * PATTERN: Template Method Pattern (Concrete Class 2)
 * 
 * RenewalEnrollment implements the specific steps for renewing an existing membership:
 * - verifyMember: Confirms member exists and has a current or past membership to renew.
 * - collectFee: Extends base collectFee with a 5% Loyalty Renewal Discount.
 * - activateMembership: Extends validity dates, updates database, records Memento snapshot.
 * - sendWelcome: Emits a personalized loyalty renewal appreciation message.
 */
@Component("RenewalEnrollment")
public class RenewalEnrollment extends EnrollmentProcess {

    private final PlanHistoryDAO planHistoryDAO;

    public RenewalEnrollment(MemberDAO memberDAO,
                             MembershipDAO membershipDAO,
                             PaymentDAO paymentDAO,
                             FeeStrategyFactory feeStrategyFactory,
                             GymMediator gymMediator,
                             PlanHistoryDAO planHistoryDAO) {
        super(memberDAO, membershipDAO, paymentDAO, feeStrategyFactory, gymMediator);
        this.planHistoryDAO = planHistoryDAO;
    }

    @Override
    protected void verifyMember(EnrollmentContext context) {
        if (context.getMember() == null || context.getMember().getId() == null) {
            throw new IllegalArgumentException("Verification failed: Member information is missing.");
        }

        Long memberId = context.getMember().getId();
        Optional<Member> memberOpt = memberDAO.findById(memberId);
        if (memberOpt.isEmpty()) {
            throw new IllegalStateException("Verification failed: Member with ID " + memberId + " does not exist.");
        }
        context.setMember(memberOpt.get());

        // Verify member has a membership history
        Optional<Membership> activeMembership = membershipDAO.findActiveByMemberId(memberId);
        if (activeMembership.isEmpty()) {
            // Check if they had any prior membership
            if (membershipDAO.findByMemberId(memberId).isEmpty()) {
                throw new IllegalStateException("Verification failed: Member has no previous membership to renew. Please use 'New Enrollment'.");
            }
        }
    }

    @Override
    protected void collectFee(EnrollmentContext context) {
        // Run standard Strategy calculation
        super.collectFee(context);

        // Apply a 5% Loyalty Renewal Discount
        double standardTotal = context.getMembership().getTotalFee();
        double discountedTotal = Math.round((standardTotal * 0.95) * 100.0) / 100.0;
        context.getMembership().setTotalFee(discountedTotal);

        if (context.getPayment() != null) {
            context.getPayment().setAmount(discountedTotal);
        }
    }

    @Override
    protected void activateMembership(EnrollmentContext context) {
        Long memberId = context.getMember().getId();
        Optional<Membership> activeOpt = membershipDAO.findActiveByMemberId(memberId);

        Membership target;
        LocalDate newStartDate = LocalDate.now();

        if (activeOpt.isPresent()) {
            target = activeOpt.get();
            // If renewing an active plan, extend from current end date or today
            if (target.getEndDate().isAfter(newStartDate)) {
                newStartDate = target.getEndDate().plusDays(1);
            }
            target.setPlanType(context.getMembership().getPlanType());
            target.setFeeStrategy(context.getMembership().getFeeStrategy());
            target.setBaseFee(context.getMembership().getBaseFee());
            target.setTotalFee(context.getMembership().getTotalFee());
            target.setStartDate(newStartDate);
            
            // Set extended end date based on renewed plan
            if ("YEARLY".equalsIgnoreCase(target.getPlanType())) {
                target.setEndDate(newStartDate.plusYears(1));
            } else {
                target.setEndDate(newStartDate.plusMonths(1));
            }
            target.setTrainerId(context.getMembership().getTrainerId());
            target.setLocker(context.getMembership().isLocker());
            target.setDietPlan(context.getMembership().isDietPlan());
            target.setPtSessions(context.getMembership().getPtSessions());
            target.setStatus("ACTIVE");

            membershipDAO.update(target);
            context.setMembership(target);
        } else {
            // Previous membership was expired, build new one
            Membership renewed = new Membership.Builder(memberId, context.getMembership().getPlanType(), newStartDate)
                    .trainer(context.getMembership().getTrainerId())
                    .locker(context.getMembership().isLocker())
                    .dietPlan(context.getMembership().isDietPlan())
                    .ptSessions(context.getMembership().getPtSessions())
                    .feeStrategy(context.getMembership().getFeeStrategy())
                    .baseFee(context.getMembership().getBaseFee())
                    .totalFee(context.getMembership().getTotalFee())
                    .status("ACTIVE")
                    .build();

            Long newId = membershipDAO.create(renewed);
            renewed.setId(newId);
            context.setMembership(renewed);
        }

        // Record payment
        if (context.getPayment() != null) {
            context.getPayment().setMembershipId(context.getMembership().getId());
            paymentDAO.create(context.getPayment());
        }

        // PATTERN: Memento snapshot saved to history caretaker
        MemberPlan planOriginator = new MemberPlan(
                context.getMembership().getMemberId(),
                context.getMembership().getPlanType(),
                context.getMembership().getTotalFee(),
                context.getMembership().isLocker(),
                context.getMembership().isDietPlan(),
                context.getMembership().getPtSessions(),
                "ACTIVE"
        );
        planHistoryDAO.saveSnapshot(context.getMembership().getMemberId(), "RENEWAL", planOriginator.saveToMemento());

        // Notify Mediator
        gymMediator.notify(this, "MEMBER_RENEWED", context.getMembership());
    }

    @Override
    protected void sendWelcome(EnrollmentContext context) {
        String templateNote = (context.getTemplateName() != null && !context.getTemplateName().trim().isEmpty())
                ? " (" + context.getTemplateName() + " Template)"
                : "";
        String msg = String.format("💪 Welcome back to the grind, %s! Your %s membership%s has been renewed with our 5%% Loyalty Discount. Keep crushing your fitness goals!",
                context.getMember().getName(), context.getMembership().getPlanType(), templateNote);
        context.setWelcomeMessage(msg);
        logger.info(msg);
    }
}
