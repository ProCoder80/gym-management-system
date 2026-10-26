package com.gym.service;

import com.gym.dao.PaymentDAO;
import com.gym.model.entity.Payment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentDAO paymentDAO;

    public PaymentService(PaymentDAO paymentDAO) {
        this.paymentDAO = paymentDAO;
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentDAO.findById(id);
    }

    public List<Payment> getPaymentsByMember(Long memberId) {
        return paymentDAO.findByMemberId(memberId);
    }

    public double getTotalRevenue() {
        return paymentDAO.calculateTotalRevenue();
    }
}
