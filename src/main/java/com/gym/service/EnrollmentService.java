package com.gym.service;

import com.gym.dao.MemberDAO;
import com.gym.model.dto.EnrollmentForm;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.pattern.templatemethod.EnrollmentContext;
import com.gym.pattern.templatemethod.NewEnrollment;
import com.gym.pattern.templatemethod.RenewalEnrollment;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class EnrollmentService {

    private final NewEnrollment newEnrollment;
    private final RenewalEnrollment renewalEnrollment;
    private final MemberDAO memberDAO;

    public EnrollmentService(NewEnrollment newEnrollment,
                             RenewalEnrollment renewalEnrollment,
                             MemberDAO memberDAO) {
        this.newEnrollment = newEnrollment;
        this.renewalEnrollment = renewalEnrollment;
        this.memberDAO = memberDAO;
    }

    /**
     * Executes the enrollment workflow using the Template Method pattern.
     * Selects either NewEnrollment or RenewalEnrollment based on client request.
     */
    public EnrollmentContext executeEnrollment(EnrollmentForm form) {
        Member member = memberDAO.findById(form.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + form.getMemberId()));

        LocalDate startDate = form.getStartDate() != null ? form.getStartDate() : LocalDate.now();

        Membership candidate = new Membership();
        candidate.setMemberId(member.getId());
        candidate.setPlanType(form.getPlanType());
        candidate.setStartDate(startDate);
        candidate.setTrainerId(form.getTrainerId());
        candidate.setLocker(form.isLocker());
        candidate.setDietPlan(form.isDietPlan());
        candidate.setPtSessions(form.getPtSessions());
        candidate.setFeeStrategy(form.getStrategyName());

        EnrollmentContext context = new EnrollmentContext(member, candidate, form.getEnrollmentType());
        context.setPaymentMethod(form.getPaymentMethod() != null ? form.getPaymentMethod() : "Credit Card");

        // PATTERN: Template Method execution
        if ("RENEWAL".equalsIgnoreCase(form.getEnrollmentType())) {
            return renewalEnrollment.run(context);
        } else {
            return newEnrollment.run(context);
        }
    }
}
