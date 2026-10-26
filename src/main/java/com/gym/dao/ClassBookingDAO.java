package com.gym.dao;

import com.gym.model.entity.ClassBooking;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassBookingDAO: Plain JDBC Implementation
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class ClassBookingDAO {

    private final DataSource dataSource;

    public ClassBookingDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<ClassBooking> findAll() {
        String sql = "SELECT b.id, b.class_id, b.member_id, b.booking_date, b.status, " +
                     "m.name AS member_name, c.name AS class_name " +
                     "FROM class_bookings b " +
                     "JOIN members m ON b.member_id = m.id " +
                     "JOIN gym_classes c ON b.class_id = c.id " +
                     "ORDER BY b.id DESC";
        List<ClassBooking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll in ClassBookingDAO", e);
        }
        return list;
    }

    public List<ClassBooking> findByMemberId(Long memberId) {
        String sql = "SELECT b.id, b.class_id, b.member_id, b.booking_date, b.status, " +
                     "m.name AS member_name, c.name AS class_name " +
                     "FROM class_bookings b " +
                     "JOIN members m ON b.member_id = m.id " +
                     "JOIN gym_classes c ON b.class_id = c.id " +
                     "WHERE b.member_id = ? ORDER BY b.id DESC";
        List<ClassBooking> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding bookings by memberId in ClassBookingDAO", e);
        }
        return list;
    }

    public boolean isMemberBooked(Long memberId, Long classId) {
        String sql = "SELECT COUNT(*) FROM class_bookings WHERE member_id = ? AND class_id = ? AND status = 'CONFIRMED'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            ps.setLong(2, classId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking booking in ClassBookingDAO", e);
        }
        return false;
    }

    public Long create(ClassBooking booking) {
        String sql = "INSERT INTO class_bookings (class_id, member_id, booking_date, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, booking.getClassId());
            ps.setLong(2, booking.getMemberId());
            ps.setString(3, booking.getBookingDate() != null ? booking.getBookingDate().toString() : LocalDateTime.now().toString());
            ps.setString(4, booking.getStatus() != null ? booking.getStatus() : "CONFIRMED");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    booking.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating class booking in ClassBookingDAO", e);
        }
        return null;
    }

    private ClassBooking mapRow(ResultSet rs) throws SQLException {
        ClassBooking b = new ClassBooking();
        b.setId(rs.getLong("id"));
        b.setClassId(rs.getLong("class_id"));
        b.setMemberId(rs.getLong("member_id"));
        b.setMemberName(rs.getString("member_name"));
        b.setClassName(rs.getString("class_name"));
        String dateStr = rs.getString("booking_date");
        if (dateStr != null && !dateStr.isEmpty()) {
            b.setBookingDate(LocalDateTime.parse(dateStr.replace(" ", "T")));
        }
        b.setStatus(rs.getString("status"));
        return b;
    }
}
