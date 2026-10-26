package com.gym.pattern.templatemethod;

import com.gym.model.entity.Member;
import com.gym.model.entity.Membership;
import com.gym.model.entity.Payment;

import java.util.ArrayList;
import java.util.List;

/**
 * Context payload passed through the Template Method steps.
 * Records state, execution sequence, and final outcome.
 */
public class EnrollmentContext {

    private Member member;
    private Membership membership;
    private Payment payment;
    private String enrollmentType; // NEW or RENEWAL
    private String paymentMethod = "Credit Card";
    private final List<String> stepExecutionOrder = new ArrayList<>();
    private boolean successful = false;
    private String statusMessage;
    private String welcomeMessage;

    public EnrollmentContext() {
    }

    public EnrollmentContext(Member member, Membership membership, String enrollmentType) {
        this.member = member;
        this.membership = membership;
        this.enrollmentType = enrollmentType;
    }

    public void recordStep(String stepName) {
        this.stepExecutionOrder.add(stepName);
    }

    public List<String> getStepExecutionOrder() {
        return stepExecutionOrder;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public Membership getMembership() {
        return membership;
    }

    public void setMembership(Membership membership) {
        this.membership = membership;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getEnrollmentType() {
        return enrollmentType;
    }

    public void setEnrollmentType(String enrollmentType) {
        this.enrollmentType = enrollmentType;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean isSuccessful() {
        return successful;
    }

    public void setSuccessful(boolean successful) {
        this.successful = successful;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }
}
