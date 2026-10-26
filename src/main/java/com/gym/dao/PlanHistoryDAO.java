package com.gym.dao;

import com.gym.model.entity.PlanHistoryEntry;
import com.gym.pattern.memento.MemberPlan;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * PlanHistoryDAO: Plain JDBC Implementation for Memento Caretaker Persistence
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class PlanHistoryDAO {

    private final DataSource dataSource;

    public PlanHistoryDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long saveSnapshot(Long memberId, String actionType, MemberPlan.Memento memento) {
        String sql = "INSERT INTO plan_history (member_id, action_type, plan_type, total_fee, " +
                     "locker, diet_plan, pt_sessions, status, memento_data, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, memberId);
            ps.setString(2, actionType);
            ps.setString(3, memento.getPlanType());
            ps.setDouble(4, memento.getTotalFee());
            ps.setInt(5, memento.isLocker() ? 1 : 0);
            ps.setInt(6, memento.isDietPlan() ? 1 : 0);
            ps.setInt(7, memento.getPtSessions());
            ps.setString(8, memento.getStatus());
            
            // Serialize simple representation of memento data
            String data = String.format("{\"planType\":\"%s\",\"totalFee\":%.2f,\"locker\":%b,\"dietPlan\":%b,\"ptSessions\":%d,\"status\":\"%s\"}",
                    memento.getPlanType(), memento.getTotalFee(), memento.isLocker(), memento.isDietPlan(), memento.getPtSessions(), memento.getStatus());
            ps.setString(9, data);
            ps.setString(10, memento.getTimestamp() != null ? memento.getTimestamp().toString() : LocalDateTime.now().toString());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error saving memento snapshot in PlanHistoryDAO", e);
        }
        return null;
    }

    public List<PlanHistoryEntry> findByMemberId(Long memberId) {
        String sql = "SELECT id, member_id, action_type, plan_type, total_fee, locker, diet_plan, " +
                     "pt_sessions, status, memento_data, timestamp " +
                     "FROM plan_history WHERE member_id = ? ORDER BY id DESC";
        List<PlanHistoryEntry> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding history entries in PlanHistoryDAO", e);
        }
        return list;
    }

    /**
     * Finds the most recent snapshot prior to the latest one, or the top entry to restore from.
     * When undo is performed, we want the previous state snapshot from history.
     */
    public Optional<PlanHistoryEntry> findPreviousSnapshot(Long memberId) {
        // Offset 1 gets the state before current if current is at offset 0
        String sql = "SELECT id, member_id, action_type, plan_type, total_fee, locker, diet_plan, " +
                     "pt_sessions, status, memento_data, timestamp " +
                     "FROM plan_history WHERE member_id = ? AND action_type != 'UNDO' " +
                     "ORDER BY id DESC LIMIT 1 OFFSET 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding previous snapshot in PlanHistoryDAO", e);
        }
        return Optional.empty();
    }

    public Optional<PlanHistoryEntry> findLatestSnapshot(Long memberId) {
        String sql = "SELECT id, member_id, action_type, plan_type, total_fee, locker, diet_plan, " +
                     "pt_sessions, status, memento_data, timestamp " +
                     "FROM plan_history WHERE member_id = ? ORDER BY id DESC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding latest snapshot in PlanHistoryDAO", e);
        }
        return Optional.empty();
    }

    private PlanHistoryEntry mapRow(ResultSet rs) throws SQLException {
        PlanHistoryEntry entry = new PlanHistoryEntry();
        entry.setId(rs.getLong("id"));
        entry.setMemberId(rs.getLong("member_id"));
        entry.setActionType(rs.getString("action_type"));
        entry.setPlanType(rs.getString("plan_type"));
        entry.setTotalFee(rs.getDouble("total_fee"));
        entry.setLocker(rs.getInt("locker") == 1);
        entry.setDietPlan(rs.getInt("diet_plan") == 1);
        entry.setPtSessions(rs.getInt("pt_sessions"));
        entry.setStatus(rs.getString("status"));
        entry.setMementoData(rs.getString("memento_data"));
        String dateStr = rs.getString("timestamp");
        if (dateStr != null && !dateStr.isEmpty()) {
            entry.setTimestamp(LocalDateTime.parse(dateStr.replace(" ", "T")));
        }
        return entry;
    }
}
