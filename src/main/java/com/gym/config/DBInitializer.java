package com.gym.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * DBInitializer: Bootstraps SQLite tables and seeds initial data.
 * Tables: members, trainers, memberships, gym_classes, class_bookings, payments, plan_history
 * Seeds: 5 members, 3 trainers, 4 classes.
 */
@Component
public class DBInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DBInitializer.class);

    private final DataSource dataSource;

    public DBInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing SQLite database tables and seed data...");

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign key constraints in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 1. Members Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL UNIQUE,
                    phone TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    join_date TEXT NOT NULL
                );
            """);

            // 2. Trainers Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS trainers (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    specialization TEXT NOT NULL,
                    available INTEGER NOT NULL DEFAULT 1,
                    contact TEXT NOT NULL
                );
            """);

            // 3. Memberships Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS memberships (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    plan_type TEXT NOT NULL,
                    fee_strategy TEXT NOT NULL,
                    base_fee REAL NOT NULL,
                    total_fee REAL NOT NULL,
                    start_date TEXT NOT NULL,
                    end_date TEXT NOT NULL,
                    trainer_id INTEGER,
                    locker INTEGER NOT NULL DEFAULT 0,
                    diet_plan INTEGER NOT NULL DEFAULT 0,
                    pt_sessions INTEGER NOT NULL DEFAULT 0,
                    status TEXT NOT NULL DEFAULT 'ACTIVE',
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE SET NULL
                );
            """);

            // 4. Gym Classes Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS gym_classes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    description TEXT,
                    trainer_id INTEGER NOT NULL,
                    schedule_time TEXT NOT NULL,
                    capacity INTEGER NOT NULL,
                    current_enrolled INTEGER NOT NULL DEFAULT 0,
                    FOREIGN KEY (trainer_id) REFERENCES trainers(id) ON DELETE CASCADE
                );
            """);

            // 5. Class Bookings Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS class_bookings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    class_id INTEGER NOT NULL,
                    member_id INTEGER NOT NULL,
                    booking_date TEXT NOT NULL,
                    status TEXT NOT NULL DEFAULT 'CONFIRMED',
                    FOREIGN KEY (class_id) REFERENCES gym_classes(id) ON DELETE CASCADE,
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
                );
            """);

            // 6. Payments Table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS payments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    membership_id INTEGER,
                    amount REAL NOT NULL,
                    fee_strategy TEXT NOT NULL,
                    payment_date TEXT NOT NULL,
                    payment_method TEXT NOT NULL,
                    receipt_id TEXT NOT NULL UNIQUE,
                    status TEXT NOT NULL DEFAULT 'COMPLETED',
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE,
                    FOREIGN KEY (membership_id) REFERENCES memberships(id) ON DELETE SET NULL
                );
            """);

            // 7. Plan History Table (Memento Caretaker Storage)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS plan_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    member_id INTEGER NOT NULL,
                    action_type TEXT NOT NULL,
                    plan_type TEXT NOT NULL,
                    total_fee REAL NOT NULL,
                    locker INTEGER NOT NULL,
                    diet_plan INTEGER NOT NULL,
                    pt_sessions INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    memento_data TEXT NOT NULL,
                    timestamp TEXT NOT NULL,
                    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE
                );
            """);

            // Seed initial data if tables are empty
            seedDataIfEmpty(conn);

            logger.info("Database schema initialized and verified successfully.");
        }
    }

    private void seedDataIfEmpty(Connection conn) throws Exception {
        // Check if members table already seeded
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM members")) {
            if (rs.next() && rs.getInt(1) > 0) {
                logger.info("Database tables already contain data. Skipping initial seeding.");
                return;
            }
        }

        logger.info("Seeding 5 members, 3 trainers, 4 gym classes, initial memberships, and payments...");

        // 1. Seed 5 Members
        String insertMember = "INSERT INTO members (name, email, phone, status, join_date) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertMember)) {
            String[][] members = {
                {"John Doe", "john.doe@example.com", "+1-555-0101", "ACTIVE", "2026-01-10"},
                {"Sarah Jenkins", "sarah.j@example.com", "+1-555-0102", "ACTIVE", "2026-02-15"},
                {"Michael Chang", "mchang@example.com", "+1-555-0103", "ACTIVE", "2026-03-01"},
                {"Emma Watson", "emma.w@example.com", "+1-555-0104", "ACTIVE", "2026-04-12"},
                {"David Rodriguez", "david.r@example.com", "+1-555-0105", "ACTIVE", "2026-05-20"}
            };
            for (String[] m : members) {
                ps.setString(1, m[0]);
                ps.setString(2, m[1]);
                ps.setString(3, m[2]);
                ps.setString(4, m[3]);
                ps.setString(5, m[4]);
                ps.executeUpdate();
            }
        }

        // 2. Seed 3 Trainers
        String insertTrainer = "INSERT INTO trainers (name, specialization, available, contact) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertTrainer)) {
            Object[][] trainers = {
                {"Alex Turner", "Strength & Conditioning", 1, "+1-555-0201"},
                {"Maya Patel", "Yoga & Mobility", 1, "+1-555-0202"},
                {"Marcus Brody", "HIIT & Functional Training", 1, "+1-555-0203"}
            };
            for (Object[] t : trainers) {
                ps.setString(1, (String) t[0]);
                ps.setString(2, (String) t[1]);
                ps.setInt(3, (Integer) t[2]);
                ps.setString(4, (String) t[3]);
                ps.executeUpdate();
            }
        }

        // 3. Seed 4 Gym Classes
        // Note: Class 3 (High-Octane HIIT) is configured with capacity: 12 and current_enrolled: 12 (FULL) to showcase Mediator rejection!
        String insertClass = "INSERT INTO gym_classes (name, description, trainer_id, schedule_time, capacity, current_enrolled) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertClass)) {
            Object[][] classes = {
                {"Sunrise Yoga Flow", "Invigorating morning vinyasa flow focusing on flexibility, balance, and mindful breathing.", 2L, "Mon/Wed/Fri 07:00 AM", 15, 12},
                {"Iron Powerlifting", "Heavy barbell mechanics, deadlift, squat, and bench press mastery with coach supervision.", 1L, "Tue/Thu 06:00 PM", 10, 8},
                {"High-Octane HIIT", "High-intensity interval circuits designed for metabolic conditioning and raw stamina.", 3L, "Mon/Wed 05:30 PM", 12, 12},
                {"Core & Cardio Blast", "Dynamic core stability and cardiovascular conditioning drills for all fitness levels.", 1L, "Sat 09:00 AM", 20, 14}
            };
            for (Object[] c : classes) {
                ps.setString(1, (String) c[0]);
                ps.setString(2, (String) c[1]);
                ps.setLong(3, (Long) c[2]);
                ps.setString(4, (String) c[3]);
                ps.setInt(5, (Integer) c[4]);
                ps.setInt(6, (Integer) c[5]);
                ps.executeUpdate();
            }
        }

        // 4. Seed Initial Memberships for Members 1, 2, and 3
        String insertMembership = "INSERT INTO memberships (member_id, plan_type, fee_strategy, base_fee, total_fee, start_date, end_date, trainer_id, locker, diet_plan, pt_sessions, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertMembership)) {
            // Member 1: Monthly with locker & 2 PT sessions
            ps.setLong(1, 1L);
            ps.setString(2, "MONTHLY");
            ps.setString(3, "MonthlyFee");
            ps.setDouble(4, 50.0);
            ps.setDouble(5, 125.0); // 50 + 15 (locker) + 60 (2*30 PT)
            ps.setString(6, "2026-01-10");
            ps.setString(7, "2026-02-10");
            ps.setLong(8, 1L); // Trainer Alex
            ps.setInt(9, 1);
            ps.setInt(10, 0);
            ps.setInt(11, 2);
            ps.setString(12, "ACTIVE");
            ps.executeUpdate();

            // Member 2: Yearly plan with diet plan
            ps.setLong(1, 2L);
            ps.setString(2, "YEARLY");
            ps.setString(3, "YearlyFee");
            ps.setDouble(4, 480.0);
            ps.setDouble(5, 680.0); // 480 + 200 (diet)
            ps.setString(6, "2026-02-15");
            ps.setString(7, "2027-02-15");
            ps.setLong(8, 2L); // Trainer Maya
            ps.setInt(9, 0);
            ps.setInt(10, 1);
            ps.setInt(11, 0);
            ps.setString(12, "ACTIVE");
            ps.executeUpdate();

            // Member 3: Student plan
            ps.setLong(1, 3L);
            ps.setString(2, "STUDENT");
            ps.setString(3, "StudentFee");
            ps.setDouble(4, 35.0);
            ps.setDouble(5, 45.0); // 35 + 10 (locker)
            ps.setString(6, "2026-03-01");
            ps.setString(7, "2026-04-01");
            ps.setNull(8, java.sql.Types.BIGINT);
            ps.setInt(9, 1);
            ps.setInt(10, 0);
            ps.setInt(11, 0);
            ps.setString(12, "ACTIVE");
            ps.executeUpdate();
        }

        // 5. Seed Payments for Members 1, 2, and 3
        String insertPayment = "INSERT INTO payments (member_id, membership_id, amount, fee_strategy, payment_date, payment_method, receipt_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertPayment)) {
            ps.setLong(1, 1L);
            ps.setLong(2, 1L);
            ps.setDouble(3, 125.0);
            ps.setString(4, "MonthlyFee");
            ps.setString(5, "2026-01-10T10:15:00");
            ps.setString(6, "Credit Card");
            ps.setString(7, "REC-INIT001");
            ps.setString(8, "COMPLETED");
            ps.executeUpdate();

            ps.setLong(1, 2L);
            ps.setLong(2, 2L);
            ps.setDouble(3, 680.0);
            ps.setString(4, "YearlyFee");
            ps.setString(5, "2026-02-15T14:30:00");
            ps.setString(6, "Bank Transfer");
            ps.setString(7, "REC-INIT002");
            ps.setString(8, "COMPLETED");
            ps.executeUpdate();

            ps.setLong(1, 3L);
            ps.setLong(2, 3L);
            ps.setDouble(3, 45.0);
            ps.setString(4, "StudentFee");
            ps.setString(5, "2026-03-01T09:45:00");
            ps.setString(6, "Debit Card");
            ps.setString(7, "REC-INIT003");
            ps.setString(8, "COMPLETED");
            ps.executeUpdate();
        }

        // 6. Seed Memento Plan History for Member 1 and Member 2
        String insertHistory = "INSERT INTO plan_history (member_id, action_type, plan_type, total_fee, locker, diet_plan, pt_sessions, status, memento_data, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertHistory)) {
            ps.setLong(1, 1L);
            ps.setString(2, "ENROLLMENT");
            ps.setString(3, "MONTHLY");
            ps.setDouble(4, 125.0);
            ps.setInt(5, 1);
            ps.setInt(6, 0);
            ps.setInt(7, 2);
            ps.setString(8, "ACTIVE");
            ps.setString(9, "{\"planType\":\"MONTHLY\",\"totalFee\":125.0,\"locker\":true,\"dietPlan\":false,\"ptSessions\":2,\"status\":\"ACTIVE\"}");
            ps.setString(10, "2026-01-10T10:15:00");
            ps.executeUpdate();

            ps.setLong(1, 2L);
            ps.setString(2, "ENROLLMENT");
            ps.setString(3, "YEARLY");
            ps.setDouble(4, 680.0);
            ps.setInt(5, 0);
            ps.setInt(6, 1);
            ps.setInt(7, 0);
            ps.setString(8, "ACTIVE");
            ps.setString(9, "{\"planType\":\"YEARLY\",\"totalFee\":680.0,\"locker\":false,\"dietPlan\":true,\"ptSessions\":0,\"status\":\"ACTIVE\"}");
            ps.setString(10, "2026-02-15T14:30:00");
            ps.executeUpdate();
        }

        // 7. Seed Class Booking for Member 1 in Class 1
        String insertBooking = "INSERT INTO class_bookings (class_id, member_id, booking_date, status) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertBooking)) {
            ps.setLong(1, 1L);
            ps.setLong(2, 1L);
            ps.setString(3, "2026-01-10T10:20:00");
            ps.setString(4, "CONFIRMED");
            ps.executeUpdate();
        }

        logger.info("Database seeding completed successfully.");
    }
}
