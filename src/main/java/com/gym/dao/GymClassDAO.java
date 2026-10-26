package com.gym.dao;

import com.gym.model.entity.GymClass;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * GymClassDAO: Plain JDBC Implementation
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class GymClassDAO {

    private final DataSource dataSource;

    public GymClassDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<GymClass> findAll() {
        String sql = "SELECT c.id, c.name, c.description, c.trainer_id, c.schedule_time, c.capacity, " +
                     "c.current_enrolled, t.name AS trainer_name, t.available AS trainer_available " +
                     "FROM gym_classes c " +
                     "JOIN trainers t ON c.trainer_id = t.id " +
                     "ORDER BY c.id ASC";
        List<GymClass> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll in GymClassDAO", e);
        }
        return list;
    }

    public Optional<GymClass> findById(Long id) {
        String sql = "SELECT c.id, c.name, c.description, c.trainer_id, c.schedule_time, c.capacity, " +
                     "c.current_enrolled, t.name AS trainer_name, t.available AS trainer_available " +
                     "FROM gym_classes c " +
                     "JOIN trainers t ON c.trainer_id = t.id " +
                     "WHERE c.id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById in GymClassDAO", e);
        }
        return Optional.empty();
    }

    public Optional<GymClass> findDefaultAvailableClass() {
        String sql = "SELECT c.id, c.name, c.description, c.trainer_id, c.schedule_time, c.capacity, " +
                     "c.current_enrolled, t.name AS trainer_name, t.available AS trainer_available " +
                     "FROM gym_classes c " +
                     "JOIN trainers t ON c.trainer_id = t.id " +
                     "WHERE c.current_enrolled < c.capacity AND t.available = 1 " +
                     "ORDER BY c.id ASC LIMIT 1";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding default available class in GymClassDAO", e);
        }
        return Optional.empty();
    }

    public boolean incrementEnrolled(Long id) {
        String sql = "UPDATE gym_classes SET current_enrolled = current_enrolled + 1 WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error incrementing enrolled count in GymClassDAO", e);
        }
    }

    public boolean decrementEnrolled(Long id) {
        String sql = "UPDATE gym_classes SET current_enrolled = MAX(0, current_enrolled - 1) WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error decrementing enrolled count in GymClassDAO", e);
        }
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM gym_classes";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting classes in GymClassDAO", e);
        }
        return 0;
    }

    private GymClass mapRow(ResultSet rs) throws SQLException {
        GymClass gc = new GymClass();
        gc.setId(rs.getLong("id"));
        gc.setName(rs.getString("name"));
        gc.setDescription(rs.getString("description"));
        gc.setTrainerId(rs.getLong("trainer_id"));
        gc.setScheduleTime(rs.getString("schedule_time"));
        gc.setCapacity(rs.getInt("capacity"));
        gc.setCurrentEnrolled(rs.getInt("current_enrolled"));
        gc.setTrainerName(rs.getString("trainer_name"));
        gc.setTrainerAvailable(rs.getInt("trainer_available") == 1);
        return gc;
    }
}
