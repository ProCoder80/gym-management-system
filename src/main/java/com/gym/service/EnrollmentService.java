package com.gym.service;

import com.gym.dao.MemberDAO;
import com.gym.model.dto.EnrollmentForm;
import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.model.prototype.MembershipPrototype;
import com.gym.model.prototype.MembershipPrototypeRegistry;
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
    private final MembershipPrototypeRegistry prototypeRegistry;

    public EnrollmentService(NewEnrollment newEnrollment,
                             RenewalEnrollment renewalEnrollment,
                             MemberDAO memberDAO,
                             MembershipPrototypeRegistry prototypeRegistry) {
        this.newEnrollment = newEnrollment;
        this.renewalEnrollment = renewalEnrollment;
        this.memberDAO = memberDAO;
        this.prototypeRegistry = prototypeRegistry;
    }

    /**
     * Executes the enrollment workflow using the Prototype, Builder, Strategy,
     * and Template Method patterns.
     *
     * 1. Retrieves and clones the requested membership plan template from MembershipPrototypeRegistry (Prototype Pattern).
     * 2. Customizes the clone with member-specific parameters (independent between clones).
     * 3. Constructs the validated Membership candidate (Builder Pattern).
     * 4. Evaluates pricing and records payments (Strategy Pattern).
     * 5. Runs the Template Method lifecycle (NewEnrollment or RenewalEnrollment).
     */
    public EnrollmentContext executeEnrollment(EnrollmentForm form) {
        Member member = memberDAO.findById(form.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + form.getMemberId()));

        LocalDate startDate = form.getStartDate() != null ? form.getStartDate() : LocalDate.now();

        // PATTERN: Prototype Pattern - Retrieve template and clone before customization
        String templateKey = (form.getTemplateType() != null && !form.getTemplateType().trim().isEmpty())
                ? form.getTemplateType()
                : form.getPlanType();

        if (templateKey == null || !prototypeRegistry.containsPrototype(templateKey)) {
            templateKey = form.getPlanType() != null ? form.getPlanType() : "BASIC";
        }

        MembershipPrototype prototypeClone = prototypeRegistry.getClonedPrototype(templateKey);

        // Customize the cloned prototype independently
        prototypeClone.setMemberId(member.getId());
        prototypeClone.setStartDate(startDate);
        prototypeClone.setTrainerId(form.getTrainerId());
        if (form.getPlanType() != null && !form.getPlanType().trim().isEmpty()) {
            prototypeClone.setPlanType(form.getPlanType());
        }
        if (form.getStrategyName() != null && !form.getStrategyName().trim().isEmpty()) {
            prototypeClone.setFeeStrategy(form.getStrategyName());
        }
        prototypeClone.setLocker(form.isLocker());
        prototypeClone.setDietPlan(form.isDietPlan());
        prototypeClone.setPtSessions(form.getPtSessions());

        // Convert customized prototype into Membership entity via Builder Pattern
        Membership candidate = prototypeClone.toMembership();

        EnrollmentContext context = new EnrollmentContext(member, candidate, form.getEnrollmentType());
        context.setTemplateName(prototypeClone.getTemplateName());
        context.setPaymentMethod(form.getPaymentMethod() != null ? form.getPaymentMethod() : "Credit Card");

        // PATTERN: Template Method execution
        if ("RENEWAL".equalsIgnoreCase(form.getEnrollmentType())) {
            return renewalEnrollment.run(context);
        } else {
            return newEnrollment.run(context);
        }
    }
}

