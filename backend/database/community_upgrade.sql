-- ============================================================================
-- COMMUNITY NON-DESTRUCTIVE UPGRADE
-- Safe for the shared/team database: adds report / notification / block /
-- topic-mapping / review-request support to the Community feature without
-- deleting existing posts, comments, votes, follows, reposts or saves.
-- Prerequisite core tables: community_posts, comments, community_profiles,
-- students, admins, post_topics, MUTED_AUTHORS.
-- ============================================================================

USE emotion_debugging;

-- ----------------------------------------------------------------------------
-- Post topics (community-owned taxonomy). Create if missing, then seed.
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS post_topics (
    topic_id INT AUTO_INCREMENT PRIMARY KEY,
    topic_name VARCHAR(120) NOT NULL,
    topic_description VARCHAR(255) NULL,
    icon_url VARCHAR(255) NULL,
    color_hex VARCHAR(9) NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_topic_name (topic_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO post_topics (topic_name, topic_description, color_hex) VALUES
('Học tập', 'Vấn đề học tập nói chung', '#12B2C1'),
('Tình cảm & Mối quan hệ', 'Tình cảm, gia đình, bạn bè, xã hội', '#DB2777'),
('Tài chính', 'Vấn đề tài chính, chi tiêu', '#F59E0B'),
('Sức khỏe', 'Sức khỏe thể chất và tinh thần', '#10B981'),
('Khác', 'Chủ đề khác', '#6B7280')
ON DUPLICATE KEY UPDATE
    topic_description = VALUES(topic_description),
    color_hex = VALUES(color_hex);

-- Dọn mọi topic mẫu cũ không nằm trong 5 nhóm chung ở trên (từ các lần chạy trước).
-- FK community_posts.topic_id là ON DELETE SET NULL nên bài viết cũ chỉ bị bỏ tag.
DELETE FROM post_topics WHERE topic_name NOT IN (
    'Học tập', 'Tình cảm & Mối quan hệ', 'Tài chính', 'Sức khỏe', 'Khác'
);

-- ----------------------------------------------------------------------------
-- Report tables
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS post_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    reporter_student_id INT NOT NULL,
    reason VARCHAR(500) NULL,
    status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    reviewed_by_admin_id INT NULL,
    UNIQUE KEY uq_post_reporter (post_id, reporter_student_id),
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL,
    INDEX idx_post_reports_status (status),
    INDEX idx_post_reports_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS comment_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    reporter_student_id INT NOT NULL,
    reason VARCHAR(500) NULL,
    status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    reviewed_by_admin_id INT NULL,
    UNIQUE KEY uq_comment_reporter (comment_id, reporter_student_id),
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL,
    INDEX idx_comment_reports_status (status),
    INDEX idx_comment_reports_comment (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Review requests (user asks admin to reconsider a hidden post)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS post_review_requests (
    request_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    message VARCHAR(1000) NULL,
    status ENUM('pending', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    reviewed_at DATETIME NULL,
    reviewed_by_admin_id INT NULL,
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by_admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL,
    INDEX idx_review_requests_status (status),
    INDEX idx_review_requests_post (post_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Blocked authors (hard block, distinct from MUTED_AUTHORS)
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS blocked_authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    blocked_student_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_block (student_id, blocked_student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (blocked_student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_blocked_by (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Notifications
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS community_notifications (
    notification_id INT AUTO_INCREMENT PRIMARY KEY,
    recipient_student_id INT NOT NULL,
    type ENUM(
        'post_hidden', 'post_restored', 'review_result',
        'comment', 'vote', 'follow', 'report', 'system'
    ) NOT NULL DEFAULT 'system',
    title VARCHAR(255) NOT NULL,
    body VARCHAR(1000) NULL,
    related_post_id INT NULL,
    related_comment_id INT NULL,
    is_read BOOLEAN NOT NULL DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (recipient_student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (related_post_id) REFERENCES community_posts(post_id) ON DELETE SET NULL,
    FOREIGN KEY (related_comment_id) REFERENCES comments(comment_id) ON DELETE SET NULL,
    INDEX idx_notif_recipient (recipient_student_id, is_read),
    INDEX idx_notif_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ----------------------------------------------------------------------------
-- Add columns to existing tables (idempotent via stored procedures)
-- ----------------------------------------------------------------------------
DELIMITER $$

DROP PROCEDURE IF EXISTS community_add_column_if_missing$$
CREATE PROCEDURE community_add_column_if_missing(
    IN target_table VARCHAR(64), IN target_column VARCHAR(64), IN column_definition TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = target_table
          AND COLUMN_NAME = target_column
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', target_table, '` ADD COLUMN `',
                          target_column, '` ', column_definition);
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END$$

DROP PROCEDURE IF EXISTS community_add_fk_if_missing$$
CREATE PROCEDURE community_add_fk_if_missing(
    IN constraint_name_value VARCHAR(64), IN ddl_statement TEXT
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.REFERENTIAL_CONSTRAINTS
        WHERE CONSTRAINT_SCHEMA = DATABASE()
          AND CONSTRAINT_NAME = constraint_name_value
    ) THEN
        SET @ddl = ddl_statement;
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END$$

DELIMITER ;

CALL community_add_column_if_missing('community_posts', 'topic_id', 'INT NULL');
CALL community_add_column_if_missing('community_posts', 'is_hidden', 'BOOLEAN NOT NULL DEFAULT 0');
CALL community_add_column_if_missing('community_posts', 'hidden_at', 'DATETIME NULL');
CALL community_add_column_if_missing('comments', 'is_hidden', 'BOOLEAN NOT NULL DEFAULT 0');

-- Profile music (community-owned): 1 file mp3 + tên hiển thị
CALL community_add_column_if_missing('community_profiles', 'music_url',  'VARCHAR(255) NULL');
CALL community_add_column_if_missing('community_profiles', 'music_name', 'VARCHAR(150) NULL');

CALL community_add_fk_if_missing(
    'fk_posts_topic',
    'ALTER TABLE community_posts ADD CONSTRAINT fk_posts_topic FOREIGN KEY (topic_id) REFERENCES post_topics(topic_id) ON DELETE SET NULL ON UPDATE CASCADE'
);

DROP PROCEDURE community_add_column_if_missing;
DROP PROCEDURE community_add_fk_if_missing;
