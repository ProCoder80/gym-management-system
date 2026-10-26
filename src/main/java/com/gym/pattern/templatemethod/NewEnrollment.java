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
 * PATTERN: Template Method Pattern (Concrete Class 1)
 * 
 * NewEnrollment implements the specific steps for first-time gym members:
 * - verifyMember: Confirms member identity and ensures they have no existing active membership.
 * - activateMembership: Uses Membership.Builder to construct and validate the membership,
 *                      persists membership, saves initial Memento snapshot to history,
 *                      and notifies GymMediator to assign trainer and auto-register a default class.
 * - sendWelcome: Emits a personalized onboarding welcome message.
 */
@Component("NewEnrollment")
public class NewEnrollment extends EnrollmentProcess {

    private final PlanHistoryDAO planHistoryDAO;

    public NewEnrollment(MemberDAO memberDAO,
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

        // Check that member doesn't already have an active plan
        Optional<Membership> activeMembership = membershipDAO.findActiveByMemberId(memberId);
        if (activeMembership.isPresent()) {
            throw new IllegalStateException("Verification failed: Member " + memberOpt.get().getName() +
                    " already holds an ACTIVE membership (" + activeMembership.get().getPlanType() + "). Please choose Renewal or Plan Upgrade.");
        }
    }

    @Override
    protected void activateMembership(EnrollmentContext context) {
        Membership input = context.getMembership();
        LocalDate startDate = input.getStartDate() != null ? input.getStartDate() : LocalDate.now();

        // PATTERN: Using Membership.Builder with build() validation
        Membership validatedMembership = new Membership.Builder(context.getMember().getId(), input.getPlanType(), startDate)
                .trainer(input.getTrainerId())
                .locker(input.isLocker())
                .dietPlan(input.isDietPlan())
                .ptSessions(input.getPtSessions())
                .feeStrategy(input.getFeeStrategy())
                .baseFee(input.getBaseFee())
                .totalFee(input.getTotalFee())
                .status("ACTIVE")
                .build();

        Long membershipId = membershipDAO.create(validatedMembership);
        validatedMembership.setId(membershipId);
        context.setMembership(validatedMembership);

        // Link payment to the new membership and persist
        if (context.getPayment() != null) {
            context.getPayment().setMembershipId(membershipId);
            paymentDAO.create(context.getPayment());
        }

        // PATTERN: Memento snapshot saved to history caretaker
        MemberPlan planOriginator = new MemberPlan(
                validatedMembership.getMemberId(),
                validatedMembership.getPlanType(),
                validatedMembership.getTotalFee(),
                validatedMembership.isLocker(),
                validatedMembership.isDietPlan(),
                validatedMembership.getPtSessions(),
                "ACTIVE"
        );
        planHistoryDAO.saveSnapshot(validatedMembership.getMemberId(), "ENROLLMENT", planOriginator.saveToMemento());

        // PATTERN: Notify GymMediator to assign trainer and auto-register default introductory class
        gymMediator.notify(this, "MEMBER_ENROLLED", validatedMembership);
    }

    @Override
    protected void sendWelcome(EnrollmentContext context) {
        String msg = String.format("🎉 Welcome to the Gym Family, %s! Your %s membership has been successfully activated. " +
                        "A trainer has been assigned and you are registered for your introductory class session.",
                context.getMember().getName(), context.getMembership().getPlanType());
        context.setWelcomeMessage(msg);
        logger.info(msg);
    }
}
