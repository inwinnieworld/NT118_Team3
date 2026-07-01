-- =============================================================
-- EMOTION DEBUGGING - DIAGNOSTICS PROBLEM TREE & AI SESSIONS
-- =============================================================
-- Chạy tay vào DB live:  mysql -u root -p emotion_debugging < aichat_tables.sql
-- Script idempotent: chạy lại nhiều lần không lỗi (CREATE IF NOT EXISTS + TRUNCATE seed).
-- LƯU Ý: TRUNCATE problems sẽ xóa sạch cây cũ rồi seed lại đúng dữ liệu chuẩn bên dưới.
-- =============================================================

USE emotion_debugging;

-- 1. TẠO BẢNG PROBLEMS
CREATE TABLE IF NOT EXISTS problems (
    id VARCHAR(50) PRIMARY KEY COMMENT 'Mã định danh độc nhất của vấn đề (Primary Key)',
    title VARCHAR(255) NOT NULL COMMENT 'Tên hiển thị của vấn đề',
    parent_id VARCHAR(50) NULL COMMENT 'Khóa ngoại trỏ về id của vấn đề cấp trên',
    tree_level INT NOT NULL DEFAULT 1 COMMENT 'Tầng của vấn đề (1: Nhánh lớn, 2: Nhóm lỗi, 3: Lỗi cụ thể)',
    is_leaf_node BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'TRUE nếu là lỗi cụ thể nhất để liên kết với Quest',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_problems_parent
        FOREIGN KEY (parent_id)
        REFERENCES problems(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    INDEX idx_problems_parent (parent_id),
    INDEX idx_problems_level (tree_level),
    INDEX idx_problems_leaf (is_leaf_node)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. TẠO BẢNG AI CHAT SESSIONS
-- Row chỉ được tạo khi user GỬI lượt đầu tiên (không tạo khi mở UI / bấm "+").
-- session_title: NULL khi session chưa kết thúc; sinh bởi Grok khi session kết thúc.
-- (Gộp session_title thẳng vào CREATE TABLE — đặt sau student_id — để script chạy lại không lỗi Duplicate column.)
CREATE TABLE IF NOT EXISTS ai_chat_sessions (
    session_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    session_title VARCHAR(255) NULL DEFAULT NULL COMMENT 'Tiêu đề tóm tắt, chỉ có khi session kết thúc',
    -- Nhánh (Tầng 1/2) mà user đã chọn qua Quick Reply → giới hạn RAG chỉ tìm trong nhánh này.
    focus_problem_id VARCHAR(50) NULL DEFAULT NULL COMMENT 'Nhánh scope khi user bấm gợi ý; NULL = tìm toàn cây',
    resolved_problem_id VARCHAR(50) NULL COMMENT 'Trỏ về problems(id) khi chốt lỗi',
    status ENUM('active', 'pending_feedback', 'completed', 'abandoned') NOT NULL DEFAULT 'active',
    turn_count INT NOT NULL DEFAULT 0,
    chat_history JSON NOT NULL COMMENT 'Lưu mảng các tin nhắn',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_session_student FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_session_problem FOREIGN KEY (resolved_problem_id) REFERENCES problems(id) ON DELETE SET NULL,

    INDEX idx_session_student (student_id),
    INDEX idx_session_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------------
-- ALTER an toàn cho DB LIVE (bảng đã tồn tại → CREATE IF NOT EXISTS ở trên bỏ qua,
-- nên cột mới focus_problem_id sẽ KHÔNG được thêm nếu chỉ dựa vào CREATE).
-- MySQL không hỗ trợ ADD COLUMN IF NOT EXISTS → guard qua information_schema
-- để chạy lại nhiều lần không lỗi "Duplicate column name".
-- -------------------------------------------------------------
SET @col_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'ai_chat_sessions'
      AND COLUMN_NAME = 'focus_problem_id'
);
SET @ddl = IF(@col_exists = 0,
    'ALTER TABLE ai_chat_sessions ADD COLUMN focus_problem_id VARCHAR(50) NULL DEFAULT NULL COMMENT ''Nhánh scope khi user bấm gợi ý; NULL = tìm toàn cây'' AFTER session_title',
    'SELECT 1');
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================
-- INSERT DỮ LIỆU MẪU - PROBLEM TREE (TỪ ĐỒ ÁN THỰC TẾ)
-- =============================================================

-- Xóa dữ liệu cũ (nếu có) để tránh trùng lặp khi chạy lại script
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE problems;
SET FOREIGN_KEY_CHECKS = 1;


-- -------------------------------------------------------------
-- TẦNG 1: CÁC NHÁNH LỚN (tree_level = 1, is_leaf_node = FALSE)
-- -------------------------------------------------------------
INSERT INTO problems (id, title, parent_id, tree_level, is_leaf_node) VALUES
('academic', 'Vấn đề học tập', NULL, 1, FALSE),
('relationship', 'Vấn đề Tình cảm & Mối quan hệ', NULL, 1, FALSE),
('finance', 'Vấn đề tài chính', NULL, 1, FALSE),
('health', 'Vấn đề Sức khỏe', NULL, 1, FALSE);


-- -------------------------------------------------------------
-- TẦNG 2: CÁC NHÓM LỖI (tree_level = 2, is_leaf_node = FALSE)
-- -------------------------------------------------------------
INSERT INTO problems (id, title, parent_id, tree_level, is_leaf_node) VALUES
-- Nhóm của Học tập
('academic_project', 'Vấn đề Đồ án', 'academic', 2, FALSE),
('academic_knowledge', 'Vấn đề Tiếp thu kiến thức', 'academic', 2, FALSE),
('academic_exam', 'Vấn đề Đánh giá & Thi cử', 'academic', 2, FALSE),

-- Nhóm của Tình cảm & Mối quan hệ
('rel_family', 'Vấn đề Gia đình', 'relationship', 2, FALSE),
('rel_friend', 'Vấn đề Bạn bè', 'relationship', 2, FALSE),
('rel_social', 'Vấn đề Kết nối xã hội', 'relationship', 2, FALSE),

-- Nhóm của Tài chính
('fin_income', 'Vấn đề nguồn thu', 'finance', 2, FALSE),
('fin_expense', 'Vấn đề nguồn ra', 'finance', 2, FALSE),
('fin_management', 'Vấn đề quản lý tài chính', 'finance', 2, FALSE),

-- Nhóm của Sức khỏe
('health_physical', 'Vấn đề thể chất', 'health', 2, FALSE),
('health_mental', 'Vấn đề Tinh thần', 'health', 2, FALSE);


-- -------------------------------------------------------------
-- TẦNG 3: CÁC LỖI CỤ THỂ (tree_level = 3, is_leaf_node = TRUE)
-- (ĐÂY LÀ NHỮNG LỖI AI SẼ ĐƯA VÀO TRIGGER_QUEST)
-- -------------------------------------------------------------
INSERT INTO problems (id, title, parent_id, tree_level, is_leaf_node) VALUES

-- 1. Nhánh: Vấn đề Đồ án
('project_ghosting', 'Đồng đội "ghost" tin nhắn/bỏ việc.', 'academic_project', 3, TRUE),
('project_conflict', 'Bất đồng ý kiến khi làm việc.', 'academic_project', 3, TRUE),
('project_scope', 'Nội dung đề tài quá lớn.', 'academic_project', 3, TRUE),
('project_tech_conflict', 'Xung đột về mặt kỹ thuật.', 'academic_project', 3, TRUE),
('project_misunderstand', 'Hiểu sai yêu cầu của giáo viên.', 'academic_project', 3, TRUE),
('project_report_error', 'Gặp lỗi trong giai đoạn báo cáo đồ án.', 'academic_project', 3, TRUE),

-- 2. Nhánh: Vấn đề Tiếp thu kiến thức
('knowledge_lag', 'Không bắt kịp tiến độ giảng dạy trên trường.', 'academic_knowledge', 3, TRUE),
('knowledge_abstract', 'Nội dung môn học khó hiểu, trừu tượng.', 'academic_knowledge', 3, TRUE),
('knowledge_no_docs', 'Thiếu tài liệu để tự học.', 'academic_knowledge', 3, TRUE),
('knowledge_overload', 'Lượng kiến thức cần phải tiếp thu quá nhiều.', 'academic_knowledge', 3, TRUE),
('knowledge_gap', 'Hổng kiến thức nền tảng nên khó tiếp thu.', 'academic_knowledge', 3, TRUE),

-- 3. Nhánh: Vấn đề Đánh giá & Thi cử
('exam_scholarship', 'Áp lực điểm số để lấy học bổng.', 'academic_exam', 3, TRUE),
('exam_time_management', 'Gặp sai lầm trong việc phân bổ thời gian ôn thi.', 'academic_exam', 3, TRUE),
('exam_exhaustion', 'Thức đêm ôn thi liên tục khiến cơ thể kiệt quệ.', 'academic_exam', 3, TRUE),
('exam_fail_fear', 'Áp lực rớt môn, nỗi sợ học lại.', 'academic_exam', 3, TRUE),
('exam_low_score', 'Điểm số thấp, không đúng mong đợi.', 'academic_exam', 3, TRUE),

-- 4. Nhánh: Vấn đề Gia đình
('family_expectation', 'Áp lực từ kỳ vọng quá cao từ gia đình.', 'rel_family', 3, TRUE),
('family_homesick', 'Cảm giác nhớ nhà, nhớ người thân.', 'rel_family', 3, TRUE),
('family_conflict', 'Bất đồng quan điểm hoặc định hướng với gia đình.', 'rel_family', 3, TRUE),
('family_crisis', 'Gia đình đột ngột gặp biến cố.', 'rel_family', 3, TRUE),
('family_toxic', 'Môi trường gia đình độc hại.', 'rel_family', 3, TRUE),

-- 5. Nhánh: Vấn đề Bạn bè
('friend_peer_pressure', 'Áp lực cạnh tranh từ bạn bè.', 'rel_friend', 3, TRUE),
('friend_lonely', 'Không kiếm được bạn bè cùng tần số.', 'rel_friend', 3, TRUE),
('friend_conflict', 'Gặp vấn đề, tranh cãi với bạn bè.', 'rel_friend', 3, TRUE),
('friend_isolated', 'Bị nhóm bạn cô lập, nói xấu.', 'rel_friend', 3, TRUE),
('friend_bad_influence', 'Bị lôi kéo vào những thói quen xấu.', 'rel_friend', 3, TRUE),

-- 6. Nhánh: Vấn đề Kết nối xã hội
('social_shy', 'Tự ti, không tham gia các hoạt động trước đám đông.', 'rel_social', 3, TRUE),
('social_lost', 'Phân vân, lạc lõng vì không tìm thấy cộng đồng phù hợp.', 'rel_social', 3, TRUE),
('social_network', 'Không biết cách mở rộng mạng lưới.', 'rel_social', 3, TRUE),
('social_culture', 'Cảm thấy khác biệt, nỗi sợ bất đồng văn hóa.', 'rel_social', 3, TRUE),

-- 7. Nhánh: Vấn đề nguồn thu
('income_cut', 'Chu cấp gia đình đột ngột giảm, gián đoạn.', 'fin_income', 3, TRUE),
('income_no_job', 'Không biết kiếm việc làm thêm ở chỗ nào.', 'fin_income', 3, TRUE),
('income_time_conflict', 'Thời gian làm thêm xung đột với lịch học.', 'fin_income', 3, TRUE),
('income_pressure', 'Áp lực khi phải kiếm thêm nguồn thu nhập.', 'fin_income', 3, TRUE),

-- 8. Nhánh: Vấn đề nguồn ra
('expense_living', 'Chi phí sinh hoạt thường ngày tăng cao.', 'fin_expense', 3, TRUE),
('expense_tuition', 'Học phí tăng cao mỗi năm, không chi trả nổi.', 'fin_expense', 3, TRUE),
('expense_equipment', 'Hỏng hóc về các thiết bị cá nhân, đồ dùng học tập.', 'fin_expense', 3, TRUE),
('expense_food_travel', 'Chi phí ăn uống, di chuyển tăng cao bất thường.', 'fin_expense', 3, TRUE),
('expense_empty_wallet', 'Không đủ tiền để chi trả vào cuối tháng.', 'fin_expense', 3, TRUE),

-- 9. Nhánh: Vấn đề quản lý tài chính
('manage_overspend', 'Thường xuyên chi tiêu lố tay.', 'fin_management', 3, TRUE),
('manage_skill', 'Không biết cách quản lý tài chính hiệu quả.', 'fin_management', 3, TRUE),
('manage_hard_to_learn', 'Cảm thấy kiến thức tài chính khô khan, khó tiếp cận.', 'fin_management', 3, TRUE),
('manage_no_backup', 'Không có quỹ dự phòng cho các tình huống khẩn cấp.', 'fin_management', 3, TRUE),

-- 10. Nhánh: Vấn đề thể chất
('phys_backache', 'Đau mỏi vai gáy/cột sống do ngồi máy tính lâu.', 'health_physical', 3, TRUE),
('phys_sleep', 'Tình trạng thiếu ngủ, mất cân bằng sinh học.', 'health_physical', 3, TRUE),
('phys_nutrition', 'Rối loạn chế độ dinh dưỡng, bỏ bữa thường xuyên.', 'health_physical', 3, TRUE),
('phys_lazy', 'Lối sống thụ động, lười vận động thể chất.', 'health_physical', 3, TRUE),
('phys_eye', 'Gặp vấn đề về thị lực (tăng độ cận, mỏi mắt)', 'health_physical', 3, TRUE),

-- 11. Nhánh: Vấn đề Tinh thần
('mental_anxiety', 'Cảm giác căng thẳng, lo âu kéo dài.', 'health_mental', 3, TRUE),
('mental_inferiority', 'Luôn cảm thấy bản thân chưa đủ tốt, đánh giá thấp bản thân.', 'health_mental', 3, TRUE),
('mental_empty', 'Cảm thấy trống rỗng, cạn kiệt năng lượng.', 'health_mental', 3, TRUE),
('mental_mood_swing', 'Tâm lý thay đổi bất thường, khó kiểm soát.', 'health_mental', 3, TRUE),
('mental_escapism', 'Trốn tránh thực tại, không muốn đối diện với vấn đề.', 'health_mental', 3, TRUE);

SELECT 'AI Chat tables (problems, ai_chat_sessions) ready!' AS Status;
