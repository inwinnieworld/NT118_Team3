-- ============================================
-- GIT COMMIT JOURNAL - DATABASE SCHEMA
-- ============================================

-- 1. BẢNG EMOTIONS (Từ điển Cảm xúc - Master Data)
CREATE TABLE IF NOT EXISTS emotions (
    emotion_id INT AUTO_INCREMENT PRIMARY KEY,
    emotion_name VARCHAR(50) NOT NULL UNIQUE,
    emotion_category ENUM('POSITIVE', 'NEGATIVE', 'NEUTRAL') NOT NULL,
    base_weight INT DEFAULT 1,
    icon_url VARCHAR(255),
    color_hex VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_emotion_name (emotion_name),
    INDEX idx_emotion_category (emotion_category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG COMMITS (Lưu trữ từng commit)
CREATE TABLE IF NOT EXISTS commits (
    commit_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    emotion_id INT NOT NULL,
    branch_type ENUM('main', 'quest') NOT NULL DEFAULT 'main',
    user_quest_id INT NULL,
    intensity_level INT NOT NULL CHECK (intensity_level BETWEEN 0 AND 100),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (emotion_id) REFERENCES emotions(emotion_id) ON DELETE RESTRICT,
    
    INDEX idx_student_branch (student_id, branch_type),
    INDEX idx_created_at (created_at),
    INDEX idx_student_date (student_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG DAILY_MERGES (Snapshot tổng kết cuối ngày)
CREATE TABLE IF NOT EXISTS daily_merges (
    merge_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    merge_date DATE NOT NULL,
    dominant_emotion_id INT NOT NULL,
    emotion_stats JSON NOT NULL,
    user_retrospective TEXT,
    is_auto_merged BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY unique_daily_merge (student_id, merge_date),
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (dominant_emotion_id) REFERENCES emotions(emotion_id) ON DELETE RESTRICT,
    
    INDEX idx_student_date (student_id, merge_date),
    INDEX idx_merge_date (merge_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG SEVERITY_ALERTS (Lưu lịch sử cảnh báo)
CREATE TABLE IF NOT EXISTS severity_alerts (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    branch_type ENUM('main', 'quest') NOT NULL,
    alert_type ENUM('HIGH_SEVERITY', 'QUEST_INEFFECTIVE') NOT NULL,
    severity_score DECIMAL(5,2) NOT NULL,
    alert_message TEXT,
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (student_id) REFERENCES users(user_id) ON DELETE CASCADE,
    
    INDEX idx_student_triggered (student_id, triggered_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
