-- =====================================================================================
-- COMMUNITY FEATURE - COMPLETE & OPTIMIZED SCHEMA
-- Thiết kế chuẩn chỉnh, tách biệt hoàn toàn với Error Log System
-- =====================================================================================

USE emotion_debugging;

-- =====================================================================================
-- PHẦN 1: CÁC BẢNG CHÍNH (CORE TABLES)
-- =====================================================================================

-- --------------------------------------------------------
-- 1. COMMUNITY_POSTS: Bảng bài viết cộng đồng
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS COMMUNITY_POSTS (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    is_anonymous TINYINT(1) DEFAULT 0 COMMENT '0 = công khai, 1 = ẩn danh',
    view_count INT DEFAULT 0,
    is_pinned TINYINT(1) DEFAULT 0 COMMENT 'Bài viết được ghim lên đầu',
    is_locked TINYINT(1) DEFAULT 0 COMMENT 'Khóa bình luận',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_created_at (created_at),
    INDEX idx_view_count (view_count),
    INDEX idx_pinned (is_pinned, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bảng bài viết cộng đồng - Tách biệt hoàn toàn với Error Log';

-- --------------------------------------------------------
-- 2. POST_VOTES: Bảng vote bài viết (upvote/downvote)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS POST_VOTES (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    vote_type ENUM('upvote', 'downvote') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_post_vote (post_id, student_id) COMMENT 'Mỗi user chỉ vote 1 lần cho 1 bài',
    FOREIGN KEY (post_id) REFERENCES COMMUNITY_POSTS(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    INDEX idx_post_vote_type (post_id, vote_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vote bài viết - Mỗi user chỉ vote 1 lần';

-- --------------------------------------------------------
-- 3. COMMENTS: Bảng bình luận (hỗ trợ nested reply)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS COMMENTS (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    parent_comment_id INT DEFAULT NULL COMMENT 'NULL = comment gốc, NOT NULL = reply',
    content TEXT NOT NULL,
    is_anonymous TINYINT(1) DEFAULT 0,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES COMMUNITY_POSTS(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES COMMENTS(comment_id) ON DELETE CASCADE,
    INDEX idx_post (post_id),
    INDEX idx_parent (parent_comment_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Bình luận với hỗ trợ reply (nested comments)';

-- --------------------------------------------------------
-- 4. COMMENT_VOTES: Bảng vote bình luận
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS COMMENT_VOTES (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    student_id INT NOT NULL,
    vote_type ENUM('upvote', 'downvote') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_comment_vote (comment_id, student_id),
    FOREIGN KEY (comment_id) REFERENCES COMMENTS(comment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    INDEX idx_comment_vote_type (comment_id, vote_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vote bình luận';

-- =====================================================================================
-- PHẦN 2: CÁC BẢNG PHỤ (AUXILIARY TABLES)
-- =====================================================================================

-- --------------------------------------------------------
-- 5. SAVED_POSTS: Bảng lưu bài viết yêu thích
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS SAVED_POSTS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    post_id INT NOT NULL,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_saved (student_id, post_id),
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES COMMUNITY_POSTS(post_id) ON DELETE CASCADE,
    INDEX idx_student_saved (student_id, saved_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Lưu bài viết yêu thích';

-- --------------------------------------------------------
-- 6. MUTED_AUTHORS: Bảng chặn/ẩn tác giả
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS MUTED_AUTHORS (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL COMMENT 'Người chặn',
    muted_student_id INT NOT NULL COMMENT 'Người bị chặn',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_muted (student_id, muted_student_id),
    FOREIGN KEY (student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    FOREIGN KEY (muted_student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    INDEX idx_student_muted (student_id),
    CHECK (student_id != muted_student_id) COMMENT 'Không thể tự chặn mình'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Chặn tác giả - Không hiển thị bài viết của người bị chặn';

-- --------------------------------------------------------
-- 7. POST_TOPICS: Bảng chủ đề bài viết (KHÔNG PHẢI TAG TRACE ERROR!)
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS POST_TOPICS (
    topic_id INT AUTO_INCREMENT PRIMARY KEY,
    topic_name VARCHAR(50) NOT NULL UNIQUE COMMENT 'VD: Học tập, Tâm lý, Công nghệ',
    topic_description TEXT,
    icon_url VARCHAR(255),
    color_hex VARCHAR(10),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_topic_name (topic_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Chủ đề bài viết - TÁCH BIỆT với tag_core_name của TRACE_OPTIONS';

-- --------------------------------------------------------
-- 8. POST_TOPIC_MAPPING: Liên kết bài viết với chủ đề
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS POST_TOPIC_MAPPING (
    post_id INT NOT NULL,
    topic_id INT NOT NULL,
    
    PRIMARY KEY (post_id, topic_id),
    FOREIGN KEY (post_id) REFERENCES COMMUNITY_POSTS(post_id) ON DELETE CASCADE,
    FOREIGN KEY (topic_id) REFERENCES POST_TOPICS(topic_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Một bài viết có thể thuộc nhiều chủ đề';

-- --------------------------------------------------------
-- 9. POST_REPORTS: Bảng báo cáo vi phạm
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS POST_REPORTS (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    reporter_student_id INT NOT NULL,
    report_reason ENUM('spam', 'harassment', 'inappropriate', 'misinformation', 'other') NOT NULL,
    report_detail TEXT,
    status ENUM('pending', 'reviewed', 'resolved', 'dismissed') DEFAULT 'pending',
    reviewed_by_admin_id INT NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES COMMUNITY_POSTS(post_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Báo cáo bài viết vi phạm';

-- --------------------------------------------------------
-- 10. COMMENT_REPORTS: Bảng báo cáo bình luận vi phạm
-- --------------------------------------------------------
CREATE TABLE IF NOT EXISTS COMMENT_REPORTS (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    reporter_student_id INT NOT NULL,
    report_reason ENUM('spam', 'harassment', 'inappropriate', 'misinformation', 'other') NOT NULL,
    report_detail TEXT,
    status ENUM('pending', 'reviewed', 'resolved', 'dismissed') DEFAULT 'pending',
    reviewed_by_admin_id INT NULL,
    reviewed_at DATETIME NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (comment_id) REFERENCES COMMENTS(comment_id) ON DELETE CASCADE,
    FOREIGN KEY (reporter_student_id) REFERENCES STUDENTS(student_id) ON DELETE CASCADE,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Báo cáo bình luận vi phạm';

-- =====================================================================================
-- PHẦN 3: DỮ LIỆU MẪU (SEED DATA)
-- =====================================================================================

-- 1. Tạo các chủ đề (Topics) - KHÔNG PHẢI TAG TRACE ERROR
INSERT INTO POST_TOPICS (topic_name, topic_description, color_hex) VALUES
('Học tập', 'Chia sẻ kinh nghiệm học tập, tips học hiệu quả', '#4CAF50'),
('Tâm lý', 'Tâm sự, chia sẻ cảm xúc, hỗ trợ tinh thần', '#2196F3'),
('Công nghệ', 'Thảo luận về công nghệ, lập trình, tools', '#FF9800'),
('Sự kiện', 'Thông tin về sự kiện, workshop, seminar', '#9C27B0'),
('Tìm việc', 'Chia sẻ cơ hội việc làm, kinh nghiệm phỏng vấn', '#F44336'),
('Giải trí', 'Giải trí, thư giãn, hoạt động ngoại khóa', '#00BCD4');

-- 2. Tạo bài viết mẫu
INSERT INTO COMMUNITY_POSTS (student_id, title, content, is_anonymous, view_count, is_pinned) VALUES
(1, 'Làm sao để vượt qua stress khi deadline?', 
 'Mình đang rất stress vì nhiều deadline cùng lúc. Các bạn có tips gì không? Mình thử Pomodoro rồi nhưng vẫn không hiệu quả lắm.', 
 0, 15, 0),
 
(2, 'Chia sẻ kinh nghiệm học lập trình hiệu quả', 
 'Sau 2 năm học, mình rút ra được vài bài học:\n1. Làm project thực tế\n2. Đọc code người khác\n3. Tham gia cộng đồng\nHy vọng giúp ích được các bạn!', 
 0, 42, 1),
 
(3, 'Cảm thấy mình không đủ giỏi', 
 'Thấy bạn bè giỏi quá, mình cảm thấy tự ti. Không biết có ai giống mình không? Mình học chậm hơn người khác rất nhiều.', 
 1, 28, 0),
 
(1, 'Review khóa học Machine Learning trên Coursera', 
 'Vừa hoàn thành khóa học ML của Andrew Ng. Đây là review chi tiết:\n- Nội dung: 9/10\n- Độ khó: 7/10\n- Thời gian: ~3 tháng\nRất đáng học!', 
 0, 67, 0),
 
(2, '[Tìm việc] Intern Backend Developer - Startup công nghệ', 
 'Công ty mình đang tuyển intern Backend (Node.js/Python). Lương 5-7tr, flexible time. Inbox mình nếu quan tâm nhé!', 
 0, 23, 0);

-- 3. Gắn chủ đề cho bài viết
INSERT INTO POST_TOPIC_MAPPING (post_id, topic_id) VALUES
(1, 1), (1, 2),  -- Post 1: Học tập + Tâm lý
(2, 1), (2, 3),  -- Post 2: Học tập + Công nghệ
(3, 2),          -- Post 3: Tâm lý
(4, 1), (4, 3),  -- Post 4: Học tập + Công nghệ
(5, 5);          -- Post 5: Tìm việc

-- 4. Tạo votes cho bài viết
INSERT INTO POST_VOTES (post_id, student_id, vote_type) VALUES
(1, 2, 'upvote'), (1, 3, 'upvote'),
(2, 1, 'upvote'), (2, 3, 'upvote'), (2, 4, 'upvote'),
(3, 1, 'upvote'), (3, 2, 'upvote'),
(4, 2, 'upvote'), (4, 3, 'upvote'),
(5, 1, 'upvote');

-- 5. Tạo comments
INSERT INTO COMMENTS (post_id, student_id, parent_comment_id, content, is_anonymous) VALUES
-- Comments cho post 1
(1, 2, NULL, 'Mình thường dùng Pomodoro technique, giúp tập trung hơn! Bạn thử 25 phút work + 5 phút break xem sao.', 0),
(1, 3, NULL, 'Chia nhỏ công việc ra và làm từng phần một bạn nhé. Đừng nghĩ đến cả núi việc.', 0),
(1, 1, 1, 'Cảm ơn bạn! Mình sẽ thử lại với thời gian ngắn hơn.', 0),

-- Comments cho post 2
(2, 1, NULL, 'Bài viết rất hữu ích! Cảm ơn bạn đã chia sẻ. Mình đang làm project gì để practice?', 0),
(2, 3, NULL, 'Mình cũng đang áp dụng cách này, hiệu quả thật. Đặc biệt là việc đọc code người khác.', 0),
(2, 2, 4, 'Mình đang làm clone Shopee để học về e-commerce. Bạn có thể thử làm clone các app quen thuộc.', 0),

-- Comments cho post 3 (ẩn danh)
(3, 2, NULL, 'Mình cũng từng cảm thấy vậy. Đừng so sánh mình với người khác nhé! Mỗi người có tốc độ riêng.', 0),
(3, 4, NULL, 'Mỗi người có tốc độ phát triển khác nhau. Cố lên! Quan trọng là tiến bộ mỗi ngày.', 0),
(3, 3, 7, 'Cảm ơn bạn rất nhiều. Mình cảm thấy tốt hơn rồi. Sẽ cố gắng không so sánh nữa.', 1);

-- 6. Tạo votes cho comments
INSERT INTO COMMENT_VOTES (comment_id, student_id, vote_type) VALUES
(1, 1, 'upvote'), (1, 3, 'upvote'),
(2, 1, 'upvote'),
(4, 2, 'upvote'), (4, 3, 'upvote'),
(7, 1, 'upvote'), (7, 3, 'upvote');

-- 7. Tạo saved posts
INSERT INTO SAVED_POSTS (student_id, post_id) VALUES
(1, 2),  -- Student 1 lưu post 2
(2, 4),  -- Student 2 lưu post 4
(3, 2),  -- Student 3 lưu post 2
(1, 5);  -- Student 1 lưu post 5

-- =====================================================================================
-- PHẦN 4: QUERIES KIỂM TRA & THỐNG KÊ
-- =====================================================================================

-- Query 1: Xem tất cả bài viết với thống kê đầy đủ
SELECT 
    p.post_id,
    p.title,
    CASE 
        WHEN p.is_anonymous = 1 THEN 'Ẩn danh'
        ELSE u.name
    END AS author_name,
    p.view_count,
    p.is_pinned,
    COUNT(DISTINCT pv.vote_id) AS total_votes,
    SUM(CASE WHEN pv.vote_type = 'upvote' THEN 1 ELSE 0 END) AS upvotes,
    SUM(CASE WHEN pv.vote_type = 'downvote' THEN 1 ELSE 0 END) AS downvotes,
    COUNT(DISTINCT c.comment_id) AS total_comments,
    GROUP_CONCAT(DISTINCT pt.topic_name SEPARATOR ', ') AS topics,
    p.created_at
FROM COMMUNITY_POSTS p
JOIN STUDENTS st ON p.student_id = st.student_id
JOIN USERS u ON st.user_id = u.user_id
LEFT JOIN POST_VOTES pv ON p.post_id = pv.post_id
LEFT JOIN COMMENTS c ON p.post_id = c.post_id
LEFT JOIN POST_TOPIC_MAPPING ptm ON p.post_id = ptm.post_id
LEFT JOIN POST_TOPICS pt ON ptm.topic_id = pt.topic_id
GROUP BY p.post_id
ORDER BY p.is_pinned DESC, p.created_at DESC;

-- Query 2: Xem comments của một bài viết (với nested structure)
SELECT 
    c.comment_id,
    c.content,
    CASE 
        WHEN c.is_anonymous = 1 THEN 'Ẩn danh'
        ELSE u.name
    END AS author_name,
    c.parent_comment_id,
    COUNT(DISTINCT cv.vote_id) AS total_votes,
    SUM(CASE WHEN cv.vote_type = 'upvote' THEN 1 ELSE 0 END) AS upvotes,
    c.created_at
FROM COMMENTS c
JOIN STUDENTS st ON c.student_id = st.student_id
JOIN USERS u ON st.user_id = u.user_id
LEFT JOIN COMMENT_VOTES cv ON c.comment_id = cv.comment_id
WHERE c.post_id = 1
GROUP BY c.comment_id
ORDER BY c.parent_comment_id ASC, c.created_at ASC;

-- Query 3: Top bài viết hot nhất (nhiều vote + comment + view)
SELECT 
    p.post_id,
    p.title,
    p.view_count,
    COUNT(DISTINCT pv.vote_id) AS total_votes,
    COUNT(DISTINCT c.comment_id) AS total_comments,
    (COUNT(DISTINCT pv.vote_id) * 2 + COUNT(DISTINCT c.comment_id) * 3 + p.view_count * 0.1) AS hot_score
FROM COMMUNITY_POSTS p
LEFT JOIN POST_VOTES pv ON p.post_id = pv.post_id AND pv.vote_type = 'upvote'
LEFT JOIN COMMENTS c ON p.post_id = c.post_id
GROUP BY p.post_id
ORDER BY hot_score DESC
LIMIT 10;

-- Query 4: Bài viết đã lưu của một student
SELECT 
    p.post_id,
    p.title,
    u.name AS author_name,
    sp.saved_at
FROM SAVED_POSTS sp
JOIN COMMUNITY_POSTS p ON sp.post_id = p.post_id
JOIN STUDENTS st ON p.student_id = st.student_id
JOIN USERS u ON st.user_id = u.user_id
WHERE sp.student_id = 1
ORDER BY sp.saved_at DESC;

-- Query 5: Kiểm tra user đã vote bài viết chưa
SELECT 
    pv.vote_type
FROM POST_VOTES pv
WHERE pv.post_id = 1 AND pv.student_id = 1;

SELECT 'Community schema created successfully! ✅' AS status;
