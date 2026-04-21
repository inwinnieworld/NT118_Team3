-- ============================================
-- ALL-IN-ONE DATABASE SETUP
-- Run this file to create database + schema + seed data
-- Usage: mysql -u root -p < setup_all_in_one.sql
-- ============================================

-- Create database
CREATE DATABASE IF NOT EXISTS emotion_debugging CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE emotion_debugging;

-- ============================================
-- SCHEMA: Drop existing tables
-- ============================================
DROP TABLE IF EXISTS severity_alerts;
DROP TABLE IF EXISTS daily_merges;
DROP TABLE IF EXISTS commits;
DROP TABLE IF EXISTS emotions;
DROP TABLE IF EXISTS password_reset_tokens;
DROP TABLE IF EXISTS staff;
DROP TABLE IF EXISTS admins;
DROP TABLE IF EXISTS students;
DROP TABLE IF EXISTS users;

-- ============================================
-- SCHEMA: Create tables
-- ============================================

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    avatar_url VARCHAR(500),
    role ENUM('student', 'staff', 'admin') NOT NULL DEFAULT 'student',
    is_locked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_role (role),
    INDEX idx_is_locked (is_locked)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE students (
    student_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    student_code VARCHAR(20) NOT NULL UNIQUE,
    major VARCHAR(100),
    faculty VARCHAR(100),
    year_of_study INT,
    emergency_phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_student_code (student_code),
    INDEX idx_faculty (faculty),
    INDEX idx_year (year_of_study)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE staff (
    staff_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    position VARCHAR(100),
    department VARCHAR(100),
    hire_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_department (department)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    admin_role VARCHAR(50) DEFAULT 'super_admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE password_reset_tokens (
    reset_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    reset_token VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_reset_token (reset_token),
    INDEX idx_expires_at (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE emotions (
    emotion_id INT AUTO_INCREMENT PRIMARY KEY,
    emotion_name VARCHAR(50) NOT NULL UNIQUE,
    emotion_category ENUM('POSITIVE', 'NEGATIVE', 'NEUTRAL') NOT NULL,
    base_weight INT DEFAULT 1,
    icon_url VARCHAR(255),
    color_hex VARCHAR(10),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_emotion_name (emotion_name),
    INDEX idx_emotion_category (emotion_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE commits (
    commit_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    emotion_id INT NOT NULL,
    branch_type ENUM('main', 'quest') NOT NULL DEFAULT 'main',
    user_quest_id INT NULL,
    intensity_level INT NOT NULL CHECK (intensity_level BETWEEN 0 AND 100),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (emotion_id) REFERENCES emotions(emotion_id) ON DELETE RESTRICT,
    INDEX idx_student_branch (student_id, branch_type),
    INDEX idx_created_at (created_at),
    INDEX idx_student_date (student_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE daily_merges (
    merge_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    merge_date DATE NOT NULL,
    dominant_emotion_id INT NOT NULL,
    emotion_stats JSON NOT NULL COMMENT 'WEA algorithm results for all emotions',
    user_retrospective TEXT,
    is_auto_merged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_daily_merge (student_id, merge_date),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (dominant_emotion_id) REFERENCES emotions(emotion_id) ON DELETE RESTRICT,
    INDEX idx_student_date (student_id, merge_date),
    INDEX idx_merge_date (merge_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE severity_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    branch_type ENUM('main', 'quest') NOT NULL,
    alert_type ENUM('HIGH_SEVERITY', 'QUEST_INEFFECTIVE') NOT NULL,
    severity_score DECIMAL(5,2) NOT NULL,
    alert_message TEXT,
    is_acknowledged BOOLEAN DEFAULT FALSE,
    acknowledged_at TIMESTAMP NULL,
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student_triggered (student_id, triggered_at),
    INDEX idx_is_acknowledged (is_acknowledged)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SELECT 'Schema created successfully!' AS status;

-- ============================================
-- SEED DATA: Note about password hash
-- ============================================
-- ⚠️  IMPORTANT: Replace the password_hash below with actual bcrypt hash
-- Run: node backend/database/generate_seed_with_hash.js
-- Then replace '$2a$10$REPLACE_WITH_ACTUAL_HASH' with the generated hash

-- For testing, you can use this pre-generated hash for 'password123':
-- $2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y
-- (This is a placeholder - generate a real one!)

-- ============================================
-- SEED: Emotions
-- ============================================
INSERT INTO emotions (emotion_name, emotion_category, base_weight, color_hex, description) VALUES
('Ác Quỷ', 'NEGATIVE', 3, '#8B0000', 'Cảm giác tức giận, hung dữ'),
('Buồn Một Chút', 'NEGATIVE', 1, '#4682B4', 'Hơi buồn, không vui'),
('Buồn Nhiều Chút', 'NEGATIVE', 2, '#191970', 'Rất buồn, chán nản'),
('Hối Lỗi', 'NEGATIVE', 2, '#8B4513', 'Cảm thấy hối hận, ân hận'),
('Hơi Quạo', 'NEGATIVE', 2, '#FF4500', 'Bực bội, khó chịu'),
('Khinh Bỉ', 'NEGATIVE', 2, '#2F4F4F', 'Coi thường, khinh miệt'),
('Chúa Hề', 'POSITIVE', 2, '#FFD700', 'Vui vẻ, hài hước'),
('Háo Hức', 'POSITIVE', 2, '#FF69B4', 'Phấn khích, mong đợi'),
('LMAO', 'POSITIVE', 3, '#00FF00', 'Cười sảng khoái'),
('Thiên Thần', 'POSITIVE', 3, '#87CEEB', 'Thuần khiết, tốt bụng'),
('Vui Vẻ', 'POSITIVE', 2, '#FFA500', 'Vui vẻ, thoải mái'),
('Yêu Thương', 'POSITIVE', 3, '#FF1493', 'Yêu thương, quan tâm'),
('Buồn Ngủ', 'NEUTRAL', 1, '#708090', 'Mệt mỏi, buồn ngủ'),
('Suy Ngẫm', 'NEUTRAL', 1, '#9370DB', 'Suy nghĩ, trầm tư'),
('Ý Kiến', 'NEUTRAL', 1, '#20B2AA', 'Có ý kiến, quan điểm');

SELECT 'Emotions inserted!' AS status;

-- ============================================
-- SEED: Users (Admin, Staff, Students)
-- ============================================
-- ⚠️  Replace this hash with actual bcrypt hash from generate_seed_with_hash.js
SET @password_hash = '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y';

-- Admin
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Nguyễn Văn Admin', 'admin@uit.edu.vn', @password_hash, '0901234567', 'admin', FALSE);
INSERT INTO admins (user_id, admin_role) VALUES (LAST_INSERT_ID(), 'super_admin');

-- Staff
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Trần Thị Thảo', 'thangda@uit.edu.vn', @password_hash, '0902345678', 'staff', FALSE);
SET @staff1_id = LAST_INSERT_ID();

INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Lê Văn Bình', 'binhle@uit.edu.vn', @password_hash, '0903456789', 'staff', FALSE);
SET @staff2_id = LAST_INSERT_ID();

INSERT INTO staff (user_id, position, department, hire_date) VALUES
(@staff1_id, 'Giảng viên', 'Khoa Khoa học Máy tính', '2020-01-15'),
(@staff2_id, 'Trợ giảng', 'Khoa Công nghệ Phần mềm', '2021-06-01');

-- Students
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Nguyễn Văn An', 'an.nguyen@student.uit.edu.vn', @password_hash, '0904567890', 'student', FALSE);
SET @student1_id = LAST_INSERT_ID();

INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Trần Thị Bình', 'binh.tran@student.uit.edu.vn', @password_hash, '0905678901', 'student', FALSE);
SET @student2_id = LAST_INSERT_ID();

INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Lê Văn Cường', 'cuong.le@student.uit.edu.vn', @password_hash, '0906789012', 'student', FALSE);
SET @student3_id = LAST_INSERT_ID();

INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Phạm Thị Dung', 'dung.pham@student.uit.edu.vn', @password_hash, '0907890123', 'student', FALSE);
SET @student4_id = LAST_INSERT_ID();

INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Hoàng Văn Em', 'em.hoang@student.uit.edu.vn', @password_hash, '0908901234', 'student', TRUE);
SET @student5_id = LAST_INSERT_ID();

INSERT INTO students (user_id, student_code, major, faculty, year_of_study, emergency_phone) VALUES
(@student1_id, '21520001', 'Khoa học Máy tính', 'Khoa Khoa học Máy tính', 3, '0981234567'),
(@student2_id, '21520002', 'Công nghệ Phần mềm', 'Khoa Công nghệ Phần mềm', 3, '0982345678'),
(@student3_id, '21520003', 'Hệ thống Thông tin', 'Khoa Hệ thống Thông tin', 2, '0983456789'),
(@student4_id, '21520004', 'Khoa học Dữ liệu', 'Khoa Khoa học Máy tính', 2, '0984567890'),
(@student5_id, '21520005', 'An toàn Thông tin', 'Khoa An toàn Thông tin', 1, '0985678901');

SELECT 'Users inserted!' AS status;

-- ============================================
-- SEED: Sample commits (for testing)
-- ============================================
-- Student 1 commits (last 3 days)
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(1, 8, 'main', 75, 'Háo hức bắt đầu ngày mới!', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR),
(1, 13, 'main', 40, 'Đang suy nghĩ về bài tập lớn', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 10 HOUR),
(1, 5, 'main', 60, 'Hơi bực vì code bị lỗi', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 14 HOUR),
(1, 11, 'main', 80, 'Vui vì fix được bug!', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 16 HOUR),
(1, 3, 'main', 70, 'Buồn nhiều vì thi không tốt', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR),
(1, 1, 'main', 85, 'Rất tức giận với bản thân', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR),
(1, 10, 'main', 70, 'Cảm thấy được động viên', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 8 HOUR),
(1, 7, 'main', 85, 'Hôm nay vui như Chúa Hề!', NOW() - INTERVAL 2 HOUR);

-- Student 2 commits (positive)
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(2, 11, 'main', 70, 'Ngày mới vui vẻ', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR),
(2, 8, 'main', 75, 'Háo hức với dự án mới', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 14 HOUR),
(2, 12, 'main', 80, 'Yêu thương cuộc sống', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 19 HOUR);

SELECT 'Commits inserted!' AS status;

-- ============================================
-- SEED: Sample daily merges
-- ============================================
INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 3), 11, 
'{"Háo Hức": {"count": 1, "avg_intensity": 75, "impact_score": 15}, "Vui Vẻ": {"count": 1, "avg_intensity": 80, "impact_score": 16}}',
'Ngày có nhiều cảm xúc, nhưng kết thúc tốt đẹp!', FALSE);

SELECT 'Daily merges inserted!' AS status;

-- ============================================
-- SEED: Sample severity alert
-- ============================================
INSERT INTO severity_alerts (student_id, branch_type, alert_type, severity_score, alert_message, is_acknowledged) VALUES
(1, 'main', 'HIGH_SEVERITY', 68.75, 
'Phát hiện mức độ cảm xúc tiêu cực cao trong 3 ngày qua. Severity Score: 68.75. Bạn có muốn tìm kiếm hỗ trợ không?', 
FALSE);

SELECT 'Severity alerts inserted!' AS status;

-- ============================================
-- FINAL SUMMARY
-- ============================================
SELECT '========================================' AS '';
SELECT 'DATABASE SETUP COMPLETE!' AS '';
SELECT '========================================' AS '';

SELECT 
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM students) AS total_students,
    (SELECT COUNT(*) FROM staff) AS total_staff,
    (SELECT COUNT(*) FROM admins) AS total_admins,
    (SELECT COUNT(*) FROM emotions) AS total_emotions,
    (SELECT COUNT(*) FROM commits) AS total_commits,
    (SELECT COUNT(*) FROM daily_merges) AS total_merges,
    (SELECT COUNT(*) FROM severity_alerts) AS total_alerts;

SELECT '========================================' AS '';
SELECT 'TEST CREDENTIALS' AS '';
SELECT '========================================' AS '';
SELECT 'Admin:   admin@uit.edu.vn / password123' AS '';
SELECT 'Staff:   thangda@uit.edu.vn / password123' AS '';
SELECT 'Student: 21520001 / password123' AS '';
SELECT '========================================' AS '';
