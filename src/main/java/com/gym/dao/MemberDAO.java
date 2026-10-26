package com.gym.dao;

import com.gym.model.entity.Member;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MemberDAO: Plain JDBC Implementation
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class MemberDAO {

    private final DataSource dataSource;

    public MemberDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Member> findAll() {
        String sql = "SELECT id, name, email, phone, status, join_date FROM members ORDER BY id ASC";
        List<Member> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll in MemberDAO", e);
        }
        return list;
    }

    public Optional<Member> findById(Long id) {
        String sql = "SELECT id, name, email, phone, status, join_date FROM members WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById in MemberDAO", e);
        }
        return Optional.empty();
    }

    public Optional<Member> findByEmail(String email) {
        String sql = "SELECT id, name, email, phone, status, join_date FROM members WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findByEmail in MemberDAO", e);
        }
        return Optional.empty();
    }

    public Long create(Member member) {
        String sql = "INSERT INTO members (name, email, phone, status, join_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getStatus() != null ? member.getStatus() : "ACTIVE");
            ps.setString(5, member.getJoinDate() != null ? member.getJoinDate().toString() : LocalDate.now().toString());
            
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    member.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating Member in MemberDAO", e);
        }
        return null;
    }

    public boolean update(Member member) {
        String sql = "UPDATE members SET name = ?, email = ?, phone = ?, status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, member.getName());
            ps.setString(2, member.getEmail());
            ps.setString(3, member.getPhone());
            ps.setString(4, member.getStatus());
            ps.setLong(5, member.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating Member in MemberDAO", e);
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error deleting Member in MemberDAO", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM members";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting members in MemberDAO", e);
        }
        return 0;
    }

    private Member mapRow(ResultSet rs) throws SQLException {
        Member m = new Member();
        m.setId(rs.getLong("id"));
        m.setName(rs.getString("name"));
        m.setEmail(rs.getString("email"));
        m.setPhone(rs.getString("phone"));
        m.setStatus(rs.getString("status"));
        String dateStr = rs.getString("join_date");
        if (dateStr != null && !dateStr.isEmpty()) {
            m.setJoinDate(LocalDate.parse(dateStr.substring(0, 10)));
        }
        return m;
    }
}
