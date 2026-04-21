-- ============================================
-- EMOTION DEBUGGING - COMPLETE DATABASE SCHEMA
-- Designed from scratch with all features in mind
-- ============================================

-- Drop existing tables (in reverse dependency order)
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
-- 1. USERS TABLE (Core user authentication)
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

-- ============================================
-- 2. STUDENTS TABLE (Student-specific data)
-- ============================================
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

-- ============================================
-- 3. STAFF TABLE (Staff-specific data)
-- ============================================
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

-- ============================================
-- 4. ADMINS TABLE (Admin-specific data)
-- ============================================
CREATE TABLE admins (
    admin_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL UNIQUE,
    admin_role VARCHAR(50) DEFAULT 'super_admin',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- 5. PASSWORD_RESET_TOKENS TABLE
-- ============================================
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

-- ============================================
-- 6. EMOTIONS TABLE (Master data - 15 emotions)
-- ============================================
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

-- ============================================
-- 7. COMMITS TABLE (Git Journal commits)
-- ============================================
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

-- ============================================
-- 8. DAILY_MERGES TABLE (Daily emotion summary)
-- ============================================
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

-- ============================================
-- 9. SEVERITY_ALERTS TABLE (Alert history)
-- ============================================
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

-- ============================================
-- SUCCESS MESSAGE
-- ============================================
SELECT 'Database schema created successfully!' AS status;
