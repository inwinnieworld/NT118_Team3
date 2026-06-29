-- ============================================================================
-- QUEST BUILDER NON-DESTRUCTIVE UPGRADE
-- Safe for the shared/team database: creates or extends Quest Builder tables
-- without deleting quests, assignments, runs, or events.
-- Prerequisite core tables: quests, staff, admins, students, user_quests.
-- ============================================================================

USE emotion_debugging;

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

DELIMITER ;

CALL quest_add_column_if_missing('basic_engine_configs', 'parent_flow_config_id', 'INT NULL');
CALL quest_add_column_if_missing('flow_engine_configs', 'parent_flow_config_id', 'INT NULL');
CALL quest_add_column_if_missing('flow_engine_configs', 'terminal_basic_config_id', 'INT NULL');
CALL quest_add_column_if_missing('flow_engine_configs', 'terminal_flow_config_id', 'INT NULL');
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
