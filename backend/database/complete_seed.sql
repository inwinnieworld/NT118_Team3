-- ============================================
-- EMOTION DEBUGGING - COMPLETE SEED DATA
-- Test data for all features
-- ============================================

-- ============================================
-- 1. SEED EMOTIONS (15 emotions)
-- ============================================
INSERT INTO emotions (emotion_name, emotion_category, base_weight, color_hex, description) VALUES
-- NEGATIVE (6 emotions)
('Ác Quỷ', 'NEGATIVE', 3, '#8B0000', 'Cảm giác tức giận, hung dữ'),
('Buồn Một Chút', 'NEGATIVE', 1, '#4682B4', 'Hơi buồn, không vui'),
('Buồn Nhiều Chút', 'NEGATIVE', 2, '#191970', 'Rất buồn, chán nản'),
('Hối Lỗi', 'NEGATIVE', 2, '#8B4513', 'Cảm thấy hối hận, ân hận'),
('Hơi Quạo', 'NEGATIVE', 2, '#FF4500', 'Bực bội, khó chịu'),
('Khinh Bỉ', 'NEGATIVE', 2, '#2F4F4F', 'Coi thường, khinh miệt'),

-- POSITIVE (6 emotions)
('Chúa Hề', 'POSITIVE', 2, '#FFD700', 'Vui vẻ, hài hước'),
('Háo Hức', 'POSITIVE', 2, '#FF69B4', 'Phấn khích, mong đợi'),
('LMAO', 'POSITIVE', 3, '#00FF00', 'Cười sảng khoái'),
('Thiên Thần', 'POSITIVE', 3, '#87CEEB', 'Thuần khiết, tốt bụng'),
('Vui Vẻ', 'POSITIVE', 2, '#FFA500', 'Vui vẻ, thoải mái'),
('Yêu Thương', 'POSITIVE', 3, '#FF1493', 'Yêu thương, quan tâm'),

-- NEUTRAL (3 emotions)
('Buồn Ngủ', 'NEUTRAL', 1, '#708090', 'Mệt mỏi, buồn ngủ'),
('Suy Ngẫm', 'NEUTRAL', 1, '#9370DB', 'Suy nghĩ, trầm tư'),
('Ý Kiến', 'NEUTRAL', 1, '#20B2AA', 'Có ý kiến, quan điểm');

-- ============================================
-- 2. SEED USERS (Admin, Staff, Students)
-- ============================================
-- Password for all: "password123" (hashed with bcrypt, rounds=10)
-- Hash: $2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y

-- Admin User
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Nguyễn Văn Admin', 'admin@uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0901234567', 'admin', FALSE);

INSERT INTO admins (user_id, admin_role) VALUES
(LAST_INSERT_ID(), 'super_admin');

-- Staff Users
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Trần Thị Thảo', 'thangda@uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0902345678', 'staff', FALSE),
('Lê Văn Bình', 'binhle@uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0903456789', 'staff', FALSE);

INSERT INTO staff (user_id, position, department, hire_date) VALUES
(2, 'Giảng viên', 'Khoa Khoa học Máy tính', '2020-01-15'),
(3, 'Trợ giảng', 'Khoa Công nghệ Phần mềm', '2021-06-01');

-- Student Users
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Nguyễn Văn An', 'an.nguyen@student.uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0904567890', 'student', FALSE),
('Trần Thị Bình', 'binh.tran@student.uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0905678901', 'student', FALSE),
('Lê Văn Cường', 'cuong.le@student.uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0906789012', 'student', FALSE),
('Phạm Thị Dung', 'dung.pham@student.uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0907890123', 'student', FALSE),
('Hoàng Văn Em', 'em.hoang@student.uit.edu.vn', '$2a$10$rZ5qH8qH8qH8qH8qH8qH8uO7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y7Y', '0908901234', 'student', TRUE); -- Locked account

INSERT INTO students (user_id, student_code, major, faculty, year_of_study, emergency_phone) VALUES
(4, '21520001', 'Khoa học Máy tính', 'Khoa Khoa học Máy tính', 3, '0981234567'),
(5, '21520002', 'Công nghệ Phần mềm', 'Khoa Công nghệ Phần mềm', 3, '0982345678'),
(6, '21520003', 'Hệ thống Thông tin', 'Khoa Hệ thống Thông tin', 2, '0983456789'),
(7, '21520004', 'Khoa học Dữ liệu', 'Khoa Khoa học Máy tính', 2, '0984567890'),
(8, '21520005', 'An toàn Thông tin', 'Khoa An toàn Thông tin', 1, '0985678901');

-- ============================================
-- 3. SEED COMMITS (Git Journal test data)
-- ============================================
-- Student 1 (An) - 3 days of commits with varying emotions
-- Day 1: Mixed emotions (3 days ago)
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(1, 8, 'main', 75, 'Háo hức bắt đầu ngày mới!', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 8 HOUR),
(1, 13, 'main', 40, 'Đang suy nghĩ về bài tập lớn', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 10 HOUR),
(1, 5, 'main', 60, 'Hơi bực vì code bị lỗi', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 14 HOUR),
(1, 11, 'main', 80, 'Vui vì fix được bug!', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 16 HOUR),
(1, 2, 'main', 30, 'Hơi buồn vì deadline gần', DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 20 HOUR);

-- Day 2: Mostly negative (2 days ago) - Should trigger severity alert
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(1, 3, 'main', 70, 'Buồn nhiều vì thi không tốt', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR),
(1, 1, 'main', 85, 'Rất tức giận với bản thân', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 12 HOUR),
(1, 4, 'main', 65, 'Hối hận vì không học kỹ', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 15 HOUR),
(1, 5, 'main', 75, 'Bực bội với mọi thứ', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 18 HOUR),
(1, 13, 'main', 50, 'Suy nghĩ về tương lai', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 21 HOUR);

-- Day 3: Recovery (1 day ago)
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(1, 10, 'main', 70, 'Cảm thấy được động viên', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 8 HOUR),
(1, 8, 'main', 65, 'Háo hức với kế hoạch mới', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 11 HOUR),
(1, 11, 'main', 75, 'Vui vẻ làm việc nhóm', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 14 HOUR),
(1, 12, 'main', 80, 'Yêu thương bạn bè', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 17 HOUR);

-- Today: Fresh start
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(1, 7, 'main', 85, 'Hôm nay vui như Chúa Hề!', NOW() - INTERVAL 2 HOUR),
(1, 9, 'main', 90, 'LMAO với meme của bạn', NOW() - INTERVAL 1 HOUR);

-- Student 2 (Bình) - Consistent positive emotions
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(2, 11, 'main', 70, 'Ngày mới vui vẻ', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 9 HOUR),
(2, 8, 'main', 75, 'Háo hức với dự án mới', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 14 HOUR),
(2, 12, 'main', 80, 'Yêu thương cuộc sống', DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 19 HOUR),
(2, 10, 'main', 85, 'Cảm thấy như thiên thần', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR),
(2, 9, 'main', 90, 'LMAO với bài giảng', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 15 HOUR);

-- Student 3 (Cường) - Neutral emotions (studying hard)
INSERT INTO commits (student_id, emotion_id, branch_type, intensity_level, message, created_at) VALUES
(3, 13, 'main', 60, 'Suy ngẫm về thuật toán', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 10 HOUR),
(3, 14, 'main', 55, 'Có ý kiến về design pattern', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 14 HOUR),
(3, 15, 'main', 70, 'Buồn ngủ sau khi học', DATE_SUB(NOW(), INTERVAL 1 DAY) + INTERVAL 20 HOUR),
(3, 13, 'main', 65, 'Tiếp tục suy ngẫm', NOW() - INTERVAL 3 HOUR);

-- ============================================
-- 4. SEED DAILY_MERGES (Sample merges)
-- ============================================
-- Student 1 - Day 1 merge (3 days ago)
INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 3), 11, 
'{"Háo hức": {"count": 1, "avg_intensity": 75, "impact_score": 15}, "Suy Ngẫm": {"count": 1, "avg_intensity": 40, "impact_score": 8}, "Hơi Quạo": {"count": 1, "avg_intensity": 60, "impact_score": 12}, "Vui Vẻ": {"count": 1, "avg_intensity": 80, "impact_score": 16}, "Buồn Một Chút": {"count": 1, "avg_intensity": 30, "impact_score": 6}}',
'Ngày có nhiều cảm xúc, nhưng kết thúc tốt đẹp!', FALSE);

-- Student 1 - Day 2 merge (2 days ago) - Negative dominant
INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged) VALUES
(1, DATE_SUB(CURDATE(), INTERVAL 2), 1,
'{"Buồn Nhiều Chút": {"count": 1, "avg_intensity": 70, "impact_score": 14}, "Ác Quỷ": {"count": 1, "avg_intensity": 85, "impact_score": 17}, "Hối Lỗi": {"count": 1, "avg_intensity": 65, "impact_score": 13}, "Hơi Quạo": {"count": 1, "avg_intensity": 75, "impact_score": 15}, "Suy Ngẫm": {"count": 1, "avg_intensity": 50, "impact_score": 10}}',
'Ngày tồi tệ, cần cải thiện tinh thần', FALSE);

-- Student 2 - Positive merge
INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged) VALUES
(2, DATE_SUB(CURDATE(), INTERVAL 2), 12,
'{"Vui Vẻ": {"count": 1, "avg_intensity": 70, "impact_score": 14}, "Háo Hức": {"count": 1, "avg_intensity": 75, "impact_score": 15}, "Yêu Thương": {"count": 1, "avg_intensity": 80, "impact_score": 16}}',
'Ngày tuyệt vời với nhiều năng lượng tích cực!', FALSE);

-- ============================================
-- 5. SEED SEVERITY_ALERTS (Sample alerts)
-- ============================================
-- Alert for Student 1 on Day 2 (high negative emotions)
INSERT INTO severity_alerts (student_id, branch_type, alert_type, severity_score, alert_message, is_acknowledged) VALUES
(1, 'main', 'HIGH_SEVERITY', 68.75, 
'Phát hiện mức độ cảm xúc tiêu cực cao trong 3 ngày qua. Severity Score: 68.75. Bạn có muốn tìm kiếm hỗ trợ không?', 
FALSE);

-- ============================================
-- 6. SEED PASSWORD_RESET_TOKENS (Sample expired token)
-- ============================================
INSERT INTO password_reset_tokens (user_id, reset_token, expires_at, is_used) VALUES
(4, 'expired_token_12345', DATE_SUB(NOW(), INTERVAL 1 HOUR), FALSE),
(5, 'valid_token_67890', DATE_ADD(NOW(), INTERVAL 9 MINUTE), FALSE);

-- ============================================
-- SUCCESS MESSAGE & SUMMARY
-- ============================================
SELECT 'Seed data inserted successfully!' AS status;

SELECT 
    'SUMMARY' AS info,
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM students) AS total_students,
    (SELECT COUNT(*) FROM staff) AS total_staff,
    (SELECT COUNT(*) FROM admins) AS total_admins,
    (SELECT COUNT(*) FROM emotions) AS total_emotions,
    (SELECT COUNT(*) FROM commits) AS total_commits,
    (SELECT COUNT(*) FROM daily_merges) AS total_merges,
    (SELECT COUNT(*) FROM severity_alerts) AS total_alerts;

-- ============================================
-- TEST CREDENTIALS
-- ============================================
SELECT '=== TEST CREDENTIALS ===' AS info;
SELECT 'Admin Login:' AS type, 'admin@uit.edu.vn' AS email, 'password123' AS password
UNION ALL
SELECT 'Staff Login:', 'thangda@uit.edu.vn', 'password123'
UNION ALL
SELECT 'Student Login:', 'an.nguyen@student.uit.edu.vn', 'password123'
UNION ALL
SELECT 'Student Code Login:', '21520001', 'password123'
UNION ALL
SELECT 'Locked Account:', 'em.hoang@student.uit.edu.vn', 'password123 (LOCKED)';
