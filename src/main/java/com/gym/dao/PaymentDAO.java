package com.gym.dao;

import com.gym.model.entity.Payment;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PaymentDAO: Plain JDBC Implementation
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class PaymentDAO {

    private final DataSource dataSource;

    public PaymentDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Payment> findAll() {
        String sql = "SELECT p.id, p.member_id, p.membership_id, p.amount, p.fee_strategy, " +
                     "p.payment_date, p.payment_method, p.receipt_id, p.status, m.name AS member_name " +
                     "FROM payments p " +
                     "JOIN members m ON p.member_id = m.id " +
                     "ORDER BY p.id DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll in PaymentDAO", e);
        }
        return list;
    }

    public Optional<Payment> findById(Long id) {
        String sql = "SELECT p.id, p.member_id, p.membership_id, p.amount, p.fee_strategy, " +
                     "p.payment_date, p.payment_method, p.receipt_id, p.status, m.name AS member_name " +
                     "FROM payments p " +
                     "JOIN members m ON p.member_id = m.id " +
                     "WHERE p.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById in PaymentDAO", e);
        }
        return Optional.empty();
    }

    public List<Payment> findByMemberId(Long memberId) {
        String sql = "SELECT p.id, p.member_id, p.membership_id, p.amount, p.fee_strategy, " +
                     "p.payment_date, p.payment_method, p.receipt_id, p.status, m.name AS member_name " +
                     "FROM payments p " +
                     "JOIN members m ON p.member_id = m.id " +
                     "WHERE p.member_id = ? ORDER BY p.id DESC";
        List<Payment> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding payments by memberId in PaymentDAO", e);
        }
        return list;
    }

    public Long create(Payment payment) {
        String sql = "INSERT INTO payments (member_id, membership_id, amount, fee_strategy, " +
                     "payment_date, payment_method, receipt_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getMemberId());
            if (payment.getMembershipId() != null) {
                ps.setLong(2, payment.getMembershipId());
            } else {
                ps.setNull(2, Types.BIGINT);
            }
            ps.setDouble(3, payment.getAmount());
            ps.setString(4, payment.getFeeStrategy());
            ps.setString(5, payment.getPaymentDate() != null ? payment.getPaymentDate().toString() : LocalDateTime.now().toString());
            ps.setString(6, payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Credit Card");
            ps.setString(7, payment.getReceiptId());
            ps.setString(8, payment.getStatus() != null ? payment.getStatus() : "COMPLETED");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    payment.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating payment in PaymentDAO", e);
        }
        return null;
    }

    public double calculateTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0.0) FROM payments WHERE status = 'COMPLETED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calculating total revenue in PaymentDAO", e);
        }
        return 0.0;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setMemberId(rs.getLong("member_id"));
        p.setMemberName(rs.getString("member_name"));
        long mId = rs.getLong("membership_id");
        if (!rs.wasNull()) {
            p.setMembershipId(mId);
        }
        p.setAmount(rs.getDouble("amount"));
        p.setFeeStrategy(rs.getString("fee_strategy"));
        String dateStr = rs.getString("payment_date");
        if (dateStr != null && !dateStr.isEmpty()) {
            p.setPaymentDate(LocalDateTime.parse(dateStr.replace(" ", "T")));
        }
        p.setPaymentMethod(rs.getString("payment_method"));
        p.setReceiptId(rs.getString("receipt_id"));
        p.setStatus(rs.getString("status"));
        return p;
    }
}
