-- ============================================================================
-- QUEST BUILDER NON-DESTRUCTIVE UPGRADE
-- Safe for the shared/team database: creates or extends Quest Builder tables
-- without deleting quests, assignments, runs, or events.
-- Prerequisite core tables: quests, staff, admins, students, user_quests.
-- ============================================================================

USE emotion_debugging;

-- Stable three-level problem taxonomy used by AI -> Quest matching.
CREATE TABLE IF NOT EXISTS problems (
    id VARCHAR(50) PRIMARY KEY COMMENT 'Stable problem identifier used by AI mapping',
    title VARCHAR(255) NOT NULL,
    parent_id VARCHAR(50) NULL,
    tree_level INT NOT NULL DEFAULT 1,
    is_leaf_node BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_problems_parent
        FOREIGN KEY (parent_id) REFERENCES problems(id)
        ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX idx_problems_parent (parent_id),
    INDEX idx_problems_level (tree_level),
    INDEX idx_problems_leaf (is_leaf_node)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO problems (id, title, parent_id, tree_level, is_leaf_node) VALUES
('learning', 'Vấn đề học tập', NULL, 1, FALSE),
('learning_project', 'Vấn đề Đồ án', 'learning', 2, FALSE),
('learning_knowledge', 'Vấn đề Tiếp thu kiến thức', 'learning', 2, FALSE),
('learning_assessment', 'Vấn đề Đánh giá & Thi cử', 'learning', 2, FALSE),
('relationship', 'Vấn đề Tình cảm & Mối quan hệ', NULL, 1, FALSE),
('relationship_family', 'Vấn đề Gia đình', 'relationship', 2, FALSE),
('relationship_friends', 'Vấn đề Bạn bè', 'relationship', 2, FALSE),
('relationship_social', 'Vấn đề Kết nối xã hội', 'relationship', 2, FALSE),

('project_ghost_teammate', 'Đồng đội "ghost" tin nhắn/bỏ việc', 'learning_project', 3, TRUE),
('project_disagreement', 'Bất đồng ý kiến khi làm việc', 'learning_project', 3, TRUE),
('project_scope_too_large', 'Nội dung đề tài quá lớn', 'learning_project', 3, TRUE),
('project_technical_conflict', 'Xung đột về mặt kỹ thuật', 'learning_project', 3, TRUE),
('project_misunderstood_requirements', 'Hiểu sai yêu cầu của giáo viên', 'learning_project', 3, TRUE),
('project_report_problem', 'Gặp lỗi trong giai đoạn báo cáo đồ án', 'learning_project', 3, TRUE),

('knowledge_cannot_keep_up', 'Không bắt kịp tiến độ giảng dạy trên trường', 'learning_knowledge', 3, TRUE),
('knowledge_abstract_content', 'Nội dung môn học khó hiểu, trừu tượng', 'learning_knowledge', 3, TRUE),
('knowledge_lack_materials', 'Thiếu tài liệu để tự học', 'learning_knowledge', 3, TRUE),
('knowledge_overload', 'Lượng kiến thức cần phải tiếp thu quá nhiều', 'learning_knowledge', 3, TRUE),
('knowledge_foundation_gap', 'Hổng kiến thức nền tảng nên khó tiếp thu', 'learning_knowledge', 3, TRUE),

('assessment_scholarship_pressure', 'Áp lực điểm số để lấy học bổng', 'learning_assessment', 3, TRUE),
('assessment_bad_time_allocation', 'Gặp sai lầm trong việc phân bổ thời gian ôn thi', 'learning_assessment', 3, TRUE),
('assessment_sleep_deprivation', 'Thức đêm ôn thi liên tục khiến cơ thể kiệt quệ', 'learning_assessment', 3, TRUE),
('assessment_failure_fear', 'Áp lực rớt môn, nỗi sợ học lại', 'learning_assessment', 3, TRUE),
('assessment_low_score', 'Điểm số thấp, không đúng mong đợi', 'learning_assessment', 3, TRUE),

('family_high_expectations', 'Áp lực từ kỳ vọng quá cao từ gia đình', 'relationship_family', 3, TRUE),
('family_homesickness', 'Cảm giác nhớ nhà, nhớ người thân', 'relationship_family', 3, TRUE),
('family_direction_disagreement', 'Bất đồng quan điểm hoặc định hướng với gia đình', 'relationship_family', 3, TRUE),
('family_unexpected_crisis', 'Gia đình đột ngột gặp biến cố', 'relationship_family', 3, TRUE),
('family_toxic_environment', 'Môi trường gia đình độc hại', 'relationship_family', 3, TRUE),

('friends_competition_pressure', 'Áp lực cạnh tranh từ bạn bè', 'relationship_friends', 3, TRUE),
('friends_no_like_minded_people', 'Không kiếm được bạn bè cùng tần số', 'relationship_friends', 3, TRUE),
('friends_conflict', 'Gặp vấn đề, tranh cãi với bạn bè', 'relationship_friends', 3, TRUE),
('friends_exclusion', 'Bị nhóm bạn cô lập, nói xấu', 'relationship_friends', 3, TRUE),
('friends_bad_influence', 'Bị lôi kéo vào những thói quen xấu', 'relationship_friends', 3, TRUE),

('social_public_insecurity', 'Tự ti, không tham gia các hoạt động trước đám đông', 'relationship_social', 3, TRUE),
('social_no_community', 'Phân vân, lạc lõng vì không tìm thấy cộng đồng phù hợp', 'relationship_social', 3, TRUE),
('social_networking_difficulty', 'Không biết cách mở rộng mạng lưới', 'relationship_social', 3, TRUE),
('social_cultural_difference', 'Cảm thấy khác biệt, nỗi sợ bất đồng văn hóa', 'relationship_social', 3, TRUE)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    parent_id = VALUES(parent_id),
    tree_level = VALUES(tree_level),
    is_leaf_node = VALUES(is_leaf_node);

CREATE TABLE IF NOT EXISTS engines (
    engine_id INT AUTO_INCREMENT PRIMARY KEY,
    engine_name VARCHAR(120) NOT NULL,
    engine_type ENUM('FLOW', 'BASIC') NOT NULL,
    engine_subtype ENUM(
        'sequential', 'parallel', 'composite', 'quest',
        'image', 'video', 'audio', 'gesture', 'sensor', 'voice',
        'text_input', 'text', 'timer'
    ) NOT NULL,
    engine_description VARCHAR(500) NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_engine_type_subtype (engine_type, engine_subtype)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO engines (engine_name, engine_type, engine_subtype, engine_description) VALUES
('Sequential Engine', 'FLOW', 'sequential', 'Ordered transition between completed configs'),
('Parallel Engine', 'FLOW', 'parallel', 'Runs child configs concurrently and evaluates completion'),
('Frame Engine', 'FLOW', 'composite', 'Contains configs and completes with its terminal child'),
('Sub Quest Engine', 'FLOW', 'quest', 'Contains a nested quest flow'),
('Image Engine', 'BASIC', 'image', 'Displays an uploaded image'),
('Video Engine', 'BASIC', 'video', 'Plays an uploaded video'),
('Audio Engine', 'BASIC', 'audio', 'Plays uploaded audio'),
('Gesture Engine', 'BASIC', 'gesture', 'Handles tap, spam tap, swipe or hold'),
('Sensor Engine', 'BASIC', 'sensor', 'Handles device sensor actions'),
('Voice Engine', 'BASIC', 'voice', 'Handles microphone input'),
('Text Input Engine', 'BASIC', 'text_input', 'Collects text input'),
('Text Engine', 'BASIC', 'text', 'Displays text or dialogue'),
('Timer Engine', 'BASIC', 'timer', 'Completes after a duration')
ON DUPLICATE KEY UPDATE
    engine_name = VALUES(engine_name),
    engine_description = VALUES(engine_description),
    is_active = TRUE;

CREATE TABLE IF NOT EXISTS quest_versions (
    version_id INT AUTO_INCREMENT PRIMARY KEY,
    quest_id INT NOT NULL,
    version_number INT NOT NULL,
    status ENUM('draft', 'pending_review', 'approved', 'rejected', 'archived')
        NOT NULL DEFAULT 'draft',
    canvas_config JSON NULL,
    created_by_staff_id INT NULL,
    submitted_at DATETIME NULL,
    approved_at DATETIME NULL,
    reviewed_by_admin_id INT NULL,
    reviewed_at DATETIME NULL,
    review_note TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_quest_version (quest_id, version_number),
    FOREIGN KEY (quest_id) REFERENCES quests(quest_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by_staff_id) REFERENCES staff(staff_id) ON DELETE SET NULL,
    FOREIGN KEY (reviewed_by_admin_id) REFERENCES admins(admin_id) ON DELETE SET NULL,
    INDEX idx_version_quest_status (quest_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS basic_engine_configs (
    basic_config_id INT AUTO_INCREMENT PRIMARY KEY,
    version_id INT NOT NULL,
    engine_id INT NOT NULL,
    client_config_id VARCHAR(80) NOT NULL,
    parent_flow_config_id INT NULL,
    display_name VARCHAR(120) NULL,
    position_x DECIMAL(10,2) NOT NULL DEFAULT 0,
    position_y DECIMAL(10,2) NOT NULL DEFAULT 0,
    width DECIMAL(10,2) NULL,
    height DECIMAL(10,2) NULL,
    z_index INT NOT NULL DEFAULT 0,
    config JSON NOT NULL,
    completion_type ENUM(
        'auto', 'timer', 'tap', 'spam_tap', 'swipe', 'hold',
        'input_submitted', 'media_finished', 'voice_finished',
        'sensor_finished', 'manual'
    ) NOT NULL DEFAULT 'auto',
    completion_config JSON NULL,
    configured_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_basic_version_client (version_id, client_config_id),
    FOREIGN KEY (version_id) REFERENCES quest_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY (engine_id) REFERENCES engines(engine_id) ON DELETE RESTRICT,
    INDEX idx_basic_version (version_id),
    INDEX idx_basic_parent (parent_flow_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS flow_engine_configs (
    flow_config_id INT AUTO_INCREMENT PRIMARY KEY,
    version_id INT NOT NULL,
    engine_id INT NOT NULL,
    client_config_id VARCHAR(80) NOT NULL,
    parent_flow_config_id INT NULL,
    display_name VARCHAR(120) NULL,
    position_x DECIMAL(10,2) NOT NULL DEFAULT 0,
    position_y DECIMAL(10,2) NOT NULL DEFAULT 0,
    width DECIMAL(10,2) NULL,
    height DECIMAL(10,2) NULL,
    z_index INT NOT NULL DEFAULT 0,
    source_basic_config_id INT NULL,
    source_flow_config_id INT NULL,
    destination_basic_config_id INT NULL,
    destination_flow_config_id INT NULL,
    sequence_order INT NULL,
    transition_type ENUM('immediate', 'delay', 'tap', 'swipe', 'drag') NULL,
    transition_config JSON NULL,
    completion_condition ENUM('A', 'B', 'A_OR_B', 'A_AND_B', 'ANY', 'ALL') NULL,
    terminal_basic_config_id INT NULL,
    terminal_flow_config_id INT NULL,
    config JSON NOT NULL,
    configured_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_flow_version_client (version_id, client_config_id),
    UNIQUE KEY uq_flow_version_sequence (version_id, sequence_order),
    FOREIGN KEY (version_id) REFERENCES quest_versions(version_id) ON DELETE CASCADE,
    FOREIGN KEY (engine_id) REFERENCES engines(engine_id) ON DELETE RESTRICT,
    FOREIGN KEY (source_basic_config_id) REFERENCES basic_engine_configs(basic_config_id) ON DELETE SET NULL,
    FOREIGN KEY (destination_basic_config_id) REFERENCES basic_engine_configs(basic_config_id) ON DELETE SET NULL,
    FOREIGN KEY (terminal_basic_config_id) REFERENCES basic_engine_configs(basic_config_id) ON DELETE SET NULL,
    INDEX idx_flow_version (version_id),
    INDEX idx_flow_parent (parent_flow_config_id),
    INDEX idx_flow_source (source_basic_config_id, source_flow_config_id),
    INDEX idx_flow_destination (destination_basic_config_id, destination_flow_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quest_run_sessions (
    run_id INT AUTO_INCREMENT PRIMARY KEY,
    user_quest_id INT NULL,
    quest_id INT NOT NULL,
    version_id INT NOT NULL,
    student_id INT NOT NULL,
    status ENUM('in_progress', 'completed', 'abandoned', 'failed')
        NOT NULL DEFAULT 'in_progress',
    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME NULL,
    result_summary JSON NULL,
    FOREIGN KEY (user_quest_id) REFERENCES user_quests(user_quest_id) ON DELETE SET NULL,
    FOREIGN KEY (quest_id) REFERENCES quests(quest_id) ON DELETE RESTRICT,
    FOREIGN KEY (version_id) REFERENCES quest_versions(version_id) ON DELETE RESTRICT,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_run_student_status (student_id, status),
    INDEX idx_run_quest_version (quest_id, version_id),
    INDEX idx_run_started_at (started_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS quest_run_events (
    event_id INT AUTO_INCREMENT PRIMARY KEY,
    run_id INT NOT NULL,
    client_config_id VARCHAR(80) NULL,
    event_type ENUM(
        'config_started', 'config_completed', 'input_received',
        'timer_finished', 'media_finished', 'error'
    ) NOT NULL,
    payload JSON NULL,
    created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
    FOREIGN KEY (run_id) REFERENCES quest_run_sessions(run_id) ON DELETE CASCADE,
    INDEX idx_event_run_config (run_id, client_config_id),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

DELIMITER $$

DROP PROCEDURE IF EXISTS quest_add_column_if_missing$$
CREATE PROCEDURE quest_add_column_if_missing(
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

DROP PROCEDURE IF EXISTS quest_add_fk_if_missing$$
CREATE PROCEDURE quest_add_fk_if_missing(
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

DROP PROCEDURE IF EXISTS quest_make_column_nullable$$
CREATE PROCEDURE quest_make_column_nullable(
    IN target_table VARCHAR(64), IN target_column VARCHAR(64)
)
BEGIN
    DECLARE existing_column_type TEXT;
    SELECT COLUMN_TYPE INTO existing_column_type
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = target_table
      AND COLUMN_NAME = target_column
      AND IS_NULLABLE = 'NO'
    LIMIT 1;

    IF existing_column_type IS NOT NULL THEN
        SET @ddl = CONCAT('ALTER TABLE `', target_table, '` MODIFY COLUMN `',
                          target_column, '` ', existing_column_type, ' NULL');
        PREPARE statement FROM @ddl;
        EXECUTE statement;
        DEALLOCATE PREPARE statement;
    END IF;
END$$

DELIMITER ;

CALL quest_add_column_if_missing('basic_engine_configs', 'parent_flow_config_id', 'INT NULL');
CALL quest_add_column_if_missing('flow_engine_configs', 'parent_flow_config_id', 'INT NULL');
CALL quest_add_column_if_missing('flow_engine_configs', 'terminal_basic_config_id', 'INT NULL');
CALL quest_add_column_if_missing('flow_engine_configs', 'terminal_flow_config_id', 'INT NULL');
CALL quest_add_column_if_missing('quests', 'problem_id', 'VARCHAR(50) NULL');
CALL quest_add_column_if_missing('quests', 'base_priority', 'INT NOT NULL DEFAULT 10');
-- Quest Builder no longer writes error_type_id. Keep the legacy FK/column nullable
-- so old Error Log and Trace Question data remain compatible.
CALL quest_make_column_nullable('quests', 'error_type_id');
CALL quest_add_fk_if_missing(
    'fk_quests_problem',
    'ALTER TABLE quests ADD CONSTRAINT fk_quests_problem FOREIGN KEY (problem_id) REFERENCES problems(id) ON DELETE SET NULL ON UPDATE CASCADE'
);
CALL quest_add_fk_if_missing(
    'fk_basic_parent_flow',
    'ALTER TABLE basic_engine_configs ADD CONSTRAINT fk_basic_parent_flow FOREIGN KEY (parent_flow_config_id) REFERENCES flow_engine_configs(flow_config_id) ON DELETE SET NULL'
);
CALL quest_add_fk_if_missing(
    'fk_flow_parent_flow',
    'ALTER TABLE flow_engine_configs ADD CONSTRAINT fk_flow_parent_flow FOREIGN KEY (parent_flow_config_id) REFERENCES flow_engine_configs(flow_config_id) ON DELETE SET NULL'
);
CALL quest_add_fk_if_missing(
    'fk_flow_source_flow',
    'ALTER TABLE flow_engine_configs ADD CONSTRAINT fk_flow_source_flow FOREIGN KEY (source_flow_config_id) REFERENCES flow_engine_configs(flow_config_id) ON DELETE SET NULL'
);
CALL quest_add_fk_if_missing(
    'fk_flow_destination_flow',
    'ALTER TABLE flow_engine_configs ADD CONSTRAINT fk_flow_destination_flow FOREIGN KEY (destination_flow_config_id) REFERENCES flow_engine_configs(flow_config_id) ON DELETE SET NULL'
);
CALL quest_add_fk_if_missing(
    'fk_flow_terminal_flow',
    'ALTER TABLE flow_engine_configs ADD CONSTRAINT fk_flow_terminal_flow FOREIGN KEY (terminal_flow_config_id) REFERENCES flow_engine_configs(flow_config_id) ON DELETE SET NULL'
);

ALTER TABLE quest_run_events
    MODIFY created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3);

DROP PROCEDURE quest_add_column_if_missing;
DROP PROCEDURE quest_add_fk_if_missing;
DROP PROCEDURE quest_make_column_nullable;
