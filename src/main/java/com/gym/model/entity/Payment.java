package com.gym.model.entity;

import java.time.LocalDateTime;

public class Payment {
    private Long id;
    private Long memberId;
    private String memberName; // Display helper
    private Long membershipId;
    private double amount;
    private String feeStrategy;
    private LocalDateTime paymentDate;
    private String paymentMethod;
    private String receiptId;
    private String status;     // COMPLETED, PENDING, REFUNDED

    public Payment() {
    }

    public Payment(Long id, Long memberId, Long membershipId, double amount, String feeStrategy,
                   LocalDateTime paymentDate, String paymentMethod, String receiptId, String status) {
        this.id = id;
        this.memberId = memberId;
        this.membershipId = membershipId;
        this.amount = amount;
        this.feeStrategy = feeStrategy;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptId = receiptId;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public Long getMembershipId() {
        return membershipId;
    }

    public void setMembershipId(Long membershipId) {
        this.membershipId = membershipId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getFeeStrategy() {
        return feeStrategy;
    }

    public void setFeeStrategy(String feeStrategy) {
        this.feeStrategy = feeStrategy;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
