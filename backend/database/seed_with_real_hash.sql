-- ============================================
-- EMOTION DEBUGGING - SEED DATA WITH REAL HASH
-- Password: password123
-- Hash: $2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy
-- ============================================

-- ============================================
-- TRUNCATE ALL TABLES (Clear existing data)
-- ============================================
SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE severity_alerts;
TRUNCATE TABLE daily_merges;
TRUNCATE TABLE commits;
TRUNCATE TABLE emotions;
TRUNCATE TABLE password_reset_tokens;
TRUNCATE TABLE staff;
TRUNCATE TABLE admins;
TRUNCATE TABLE students;
TRUNCATE TABLE users;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'All tables truncated successfully!' AS status;

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
-- Password: password123
-- Hash: $2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy
-- ============================================

-- Admin User
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Nguyễn Văn Admin', 'admin@uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0901234567', 'admin', FALSE);

INSERT INTO admins (user_id, admin_role) VALUES
(LAST_INSERT_ID(), 'super_admin');

-- Staff Users
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Trần Thị Thảo', 'thangda@uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0902345678', 'staff', FALSE),
('Lê Văn Bình', 'binhle@uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0903456789', 'staff', FALSE),
('Nguyễn Văn Cường', 'cuong.staff@uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0903456790', 'staff', FALSE);

INSERT INTO staff (user_id, position, department, hire_date) VALUES
(2, 'Nhân Viên Tạo Quest', 'Khoa Khoa học Máy tính', '2020-01-15'),
(3, 'Giảng viên', 'Khoa Công nghệ Phần mềm', '2021-06-01'),
(4, 'Trợ giảng', 'Khoa Hệ thống Thông tin', '2022-03-10');

-- Student Users
INSERT INTO users (name, email, password_hash, phone, role, is_locked) VALUES
('Nguyễn Văn An', 'an.nguyen@student.uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0904567890', 'student', FALSE),
('Trần Thị Bình', 'binh.tran@student.uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0905678901', 'student', FALSE),
('Lê Văn Dũng', 'dung.le@student.uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0906789012', 'student', FALSE),
('Phạm Thị Em', 'em.pham@student.uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0907890123', 'student', FALSE),
('Hoàng Văn Giang', 'giang.hoang@student.uit.edu.vn', '$2b$10$w/XSW5yv.3iJsdbNMAaTlujtZdRe5Jo01i3IfaioyqGJBtdk48hQy', '0908901234', 'student', TRUE); -- Locked account

INSERT INTO students (user_id, student_code, major, faculty, year_of_study, emergency_phone) VALUES
(5, '21520001', 'Khoa học Máy tính', 'Khoa Khoa học Máy tính', 3, '0981234567'),
(6, '21520002', 'Công nghệ Phần mềm', 'Khoa Công nghệ Phần mềm', 3, '0982345678'),
(7, '21520003', 'Hệ thống Thông tin', 'Khoa Hệ thống Thông tin', 2, '0983456789'),
(8, '21520004', 'Khoa học Dữ liệu', 'Khoa Khoa học Máy tính', 2, '0984567890'),
(9, '21520005', 'An toàn Thông tin', 'Khoa An toàn Thông tin', 1, '0985678901');

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
    (SELECT COUNT(*) FROM commits) AS total_commits;

-- ============================================
-- TEST CREDENTIALS
-- ============================================
SELECT '=== TEST CREDENTIALS ===' AS info;
SELECT 'Admin Login:' AS type, 'admin@uit.edu.vn' AS email, 'password123' AS password, 'Full Access' AS note
UNION ALL
SELECT 'Staff (Quest Creator):', 'thangda@uit.edu.vn', 'password123', 'Can access Staff Dashboard'
UNION ALL
SELECT 'Staff (Giảng viên):', 'binhle@uit.edu.vn', 'password123', 'Cannot access Staff Dashboard'
UNION ALL
SELECT 'Staff (Trợ giảng):', 'cuong.staff@uit.edu.vn', 'password123', 'Cannot access Staff Dashboard'
UNION ALL
SELECT 'Student Login:', 'an.nguyen@student.uit.edu.vn', 'password123', 'Active'
UNION ALL
SELECT 'Student Code Login:', '21520001', 'password123', 'Active'
UNION ALL
SELECT 'Locked Account:', 'giang.hoang@student.uit.edu.vn', 'password123', 'LOCKED';
