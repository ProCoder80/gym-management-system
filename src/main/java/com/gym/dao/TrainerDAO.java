package com.gym.dao;

import com.gym.model.entity.Trainer;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * TrainerDAO: Plain JDBC Implementation
 * Constraint: SQL statements executed strictly using PreparedStatement.
 */
@Repository
public class TrainerDAO {

    private final DataSource dataSource;

    public TrainerDAO(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public List<Trainer> findAll() {
        String sql = "SELECT id, name, specialization, available, contact FROM trainers ORDER BY id ASC";
        List<Trainer> list = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findAll in TrainerDAO", e);
        }
        return list;
    }

    public Optional<Trainer> findById(Long id) {
        String sql = "SELECT id, name, specialization, available, contact FROM trainers WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error executing findById in TrainerDAO", e);
        }
        return Optional.empty();
    }

    public boolean updateAvailability(Long id, boolean available) {
        String sql = "UPDATE trainers SET available = ? WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, available ? 1 : 0);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating trainer availability in TrainerDAO", e);
        }
    }

    public Long create(Trainer trainer) {
        String sql = "INSERT INTO trainers (name, specialization, available, contact) VALUES (?, ?, ?, ?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, trainer.getName());
            ps.setString(2, trainer.getSpecialization());
            ps.setInt(3, trainer.isAvailable() ? 1 : 0);
            ps.setString(4, trainer.getContact());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    trainer.setId(id);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error creating trainer in TrainerDAO", e);
        }
        return null;
    }

    public int count() {
        String sql = "SELECT COUNT(*) FROM trainers";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error counting trainers in TrainerDAO", e);
        }
        return 0;
    }

    private Trainer mapRow(ResultSet rs) throws SQLException {
        Trainer t = new Trainer();
        t.setId(rs.getLong("id"));
        t.setName(rs.getString("name"));
        t.setSpecialization(rs.getString("specialization"));
        t.setAvailable(rs.getInt("available") == 1);
        t.setContact(rs.getString("contact"));
        return t;
    }
}
