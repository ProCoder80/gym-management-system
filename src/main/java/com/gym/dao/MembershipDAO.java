package com.gym.dao;

import com.gym.model.entity.Membership;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * MembershipDAO: Plain JDBC Implementation
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class MembershipDAO {

    private final DataSource dataSource;

    public MembershipDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Membership> findAll() {
        String sql = "SELECT id, member_id, plan_type, fee_strategy, base_fee, total_fee, start_date, " +
                     "end_date, trainer_id, locker, diet_plan, pt_sessions, status FROM memberships ORDER BY id DESC";
        List<Membership> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll in MembershipDAO", e);
        }
        return list;
    }

    public Optional<Membership> findById(Long id) {
        String sql = "SELECT id, member_id, plan_type, fee_strategy, base_fee, total_fee, start_date, " +
                     "end_date, trainer_id, locker, diet_plan, pt_sessions, status FROM memberships WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById in MembershipDAO", e);
        }
        return Optional.empty();
    }

    public Optional<Membership> findActiveByMemberId(Long memberId) {
        String sql = "SELECT id, member_id, plan_type, fee_strategy, base_fee, total_fee, start_date, " +
                     "end_date, trainer_id, locker, diet_plan, pt_sessions, status " +
                     "FROM memberships WHERE member_id = ? AND status = 'ACTIVE' ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding active membership in MembershipDAO", e);
        }
        return Optional.empty();
    }

    public List<Membership> findByMemberId(Long memberId) {
        String sql = "SELECT id, member_id, plan_type, fee_strategy, base_fee, total_fee, start_date, " +
                     "end_date, trainer_id, locker, diet_plan, pt_sessions, status " +
                     "FROM memberships WHERE member_id = ? ORDER BY id DESC";
        List<Membership> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding memberships by memberId in MembershipDAO", e);
        }
        return list;
    }

    public Long create(Membership m) {
        String sql = "INSERT INTO memberships (member_id, plan_type, fee_strategy, base_fee, total_fee, " +
                     "start_date, end_date, trainer_id, locker, diet_plan, pt_sessions, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, m.getMemberId());
            ps.setString(2, m.getPlanType());
            ps.setString(3, m.getFeeStrategy() != null ? m.getFeeStrategy() : "MonthlyFee");
            ps.setDouble(4, m.getBaseFee());
            ps.setDouble(5, m.getTotalFee());
            ps.setString(6, m.getStartDate().toString());
            ps.setString(7, m.getEndDate().toString());
            if (m.getTrainerId() != null) {
                ps.setLong(8, m.getTrainerId());
            } else {
                ps.setNull(8, Types.BIGINT);
            }
            ps.setInt(9, m.isLocker() ? 1 : 0);
            ps.setInt(10, m.isDietPlan() ? 1 : 0);
            ps.setInt(11, m.getPtSessions());
            ps.setString(12, m.getStatus() != null ? m.getStatus() : "ACTIVE");

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    m.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating membership in MembershipDAO", e);
        }
        return null;
    }

    public boolean update(Membership m) {
        String sql = "UPDATE memberships SET plan_type = ?, fee_strategy = ?, base_fee = ?, total_fee = ?, " +
                     "start_date = ?, end_date = ?, trainer_id = ?, locker = ?, diet_plan = ?, " +
                     "pt_sessions = ?, status = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, m.getPlanType());
            ps.setString(2, m.getFeeStrategy());
            ps.setDouble(3, m.getBaseFee());
            ps.setDouble(4, m.getTotalFee());
            ps.setString(5, m.getStartDate().toString());
            ps.setString(6, m.getEndDate().toString());
            if (m.getTrainerId() != null) {
                ps.setLong(7, m.getTrainerId());
            } else {
                ps.setNull(7, Types.BIGINT);
            }
            ps.setInt(8, m.isLocker() ? 1 : 0);
            ps.setInt(9, m.isDietPlan() ? 1 : 0);
            ps.setInt(10, m.getPtSessions());
            ps.setString(11, m.getStatus());
            ps.setLong(12, m.getId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating membership in MembershipDAO", e);
        }
    }

    public int countActive() {
        String sql = "SELECT COUNT(*) FROM memberships WHERE status = 'ACTIVE'";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting active memberships in MembershipDAO", e);
        }
        return 0;
    }

    private Membership mapRow(ResultSet rs) throws SQLException {
        Membership m = new Membership();
        m.setId(rs.getLong("id"));
        m.setMemberId(rs.getLong("member_id"));
        m.setPlanType(rs.getString("plan_type"));
        m.setFeeStrategy(rs.getString("fee_strategy"));
        m.setBaseFee(rs.getDouble("base_fee"));
        m.setTotalFee(rs.getDouble("total_fee"));
        m.setStartDate(LocalDate.parse(rs.getString("start_date").substring(0, 10)));
        m.setEndDate(LocalDate.parse(rs.getString("end_date").substring(0, 10)));
        long tId = rs.getLong("trainer_id");
        if (!rs.wasNull()) {
            m.setTrainerId(tId);
        }
        m.setLocker(rs.getInt("locker") == 1);
        m.setDietPlan(rs.getInt("diet_plan") == 1);
        m.setPtSessions(rs.getInt("pt_sessions"));
        m.setStatus(rs.getString("status"));
        return m;
    }
}
