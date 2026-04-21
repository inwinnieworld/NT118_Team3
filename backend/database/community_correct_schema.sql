-- =====================================================================================
-- COMMUNITY SYSTEM - CORRECT SCHEMA
-- Khớp 100% với Android code đã implement
-- =====================================================================================

USE emotion_debugging;

-- =====================================================================================
-- CÁC BẢNG CHÍNH
-- =====================================================================================

-- 1. BẢNG BÀI VIẾT CỘNG ĐỒNG
CREATE TABLE IF NOT EXISTS community_posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT TRUE,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student (student_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG BÌNH LUẬN
CREATE TABLE IF NOT EXISTS comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    parent_comment_id INT DEFAULT NULL,
    content TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    INDEX idx_post (post_id),
    INDEX idx_parent (parent_comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG VOTE BÀI VIẾT (KHỚP VỚI ANDROID CODE)
CREATE TABLE IF NOT EXISTS post_votes (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    vote_type VARCHAR(30) NOT NULL COMMENT 'UPVOTE_FIX hoặc REPRODUCE_ERROR',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_post_vote (post_id, student_id),
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_vote_type (post_id, vote_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Vote bài viết - UPVOTE_FIX = hữu ích, REPRODUCE_ERROR = tôi cũng gặp lỗi này';

-- 4. BẢNG VOTE BÌNH LUẬN (KHỚP VỚI ANDROID CODE)
CREATE TABLE IF NOT EXISTS comment_votes (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    student_id INT NOT NULL,
    vote_type VARCHAR(30) NOT NULL COMMENT 'UPVOTE_FIX hoặc REPRODUCE_ERROR',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_comment_vote (comment_id, student_id),
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_vote_type (comment_id, vote_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG LƯU BÀI VIẾT
CREATE TABLE IF NOT EXISTS saved_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    post_id INT NOT NULL,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_saved (student_id, post_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    INDEX idx_student_saved (student_id, saved_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG CHẶN TÁC GIẢ
CREATE TABLE IF NOT EXISTS muted_authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    muted_student_id INT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uq_muted (student_id, muted_student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (muted_student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student_muted (student_id),
    CHECK (student_id != muted_student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================================
-- DỮ LIỆU MẪU (KHỚP VỚI ANDROID CODE)
-- =====================================================================================

INSERT INTO community_posts (student_id, title, content, is_anonymous, view_count) VALUES
(1, 'Làm sao để vượt qua stress khi deadline?', 
 'Mình đang rất stress vì nhiều deadline cùng lúc. Các bạn có tips gì không?', 
 0, 15),
 
(2, 'Chia sẻ kinh nghiệm học lập trình hiệu quả', 
 'Sau 2 năm học, mình rút ra được vài bài học. Hy vọng giúp ích được các bạn!', 
 0, 42),
 
(3, 'Cảm thấy mình không đủ giỏi', 
 'Thấy bạn bè giỏi quá, mình cảm thấy tự ti. Không biết có ai giống mình không?', 
 1, 28);

-- Votes (ĐÚNG THEO ANDROID CODE)
INSERT INTO post_votes (post_id, student_id, vote_type) VALUES
(1, 2, 'UPVOTE_FIX'),
(1, 3, 'UPVOTE_FIX'),
(2, 1, 'UPVOTE_FIX'),
(2, 3, 'UPVOTE_FIX'),
(3, 1, 'REPRODUCE_ERROR'),  -- Tôi cũng gặp lỗi này!
(3, 2, 'UPVOTE_FIX');

-- Comments
INSERT INTO comments (post_id, student_id, parent_comment_id, content, is_anonymous) VALUES
(1, 2, NULL, 'Mình thường dùng Pomodoro technique!', 0),
(1, 3, NULL, 'Chia nhỏ công việc ra bạn nhé', 0),
(1, 1, 1, 'Cảm ơn bạn! Mình sẽ thử', 0),
(2, 1, NULL, 'Bài viết rất hữu ích!', 0),
(3, 2, NULL, 'Mình cũng từng cảm thấy vậy', 0);

-- Comment votes (ĐÚNG THEO ANDROID CODE)
INSERT INTO comment_votes (comment_id, student_id, vote_type) VALUES
(1, 1, 'UPVOTE_FIX'),
(1, 3, 'UPVOTE_FIX'),
(2, 1, 'UPVOTE_FIX');

-- Saved posts
INSERT INTO saved_posts (student_id, post_id) VALUES
(1, 2),
(2, 3);

-- =====================================================================================
-- QUERIES (CẬP NHẬT THEO VOTE_TYPE MỚI)
-- =====================================================================================

-- Query 1: Lấy bài viết với vote count
SELECT 
    cp.post_id,
    cp.title,
    CASE WHEN cp.is_anonymous = 1 THEN 'Ẩn danh' ELSE u.name END AS author,
    cp.view_count,
    COUNT(DISTINCT CASE WHEN v.vote_type = 'UPVOTE_FIX' THEN v.vote_id END) AS upvote_count,
    COUNT(DISTINCT CASE WHEN v.vote_type = 'REPRODUCE_ERROR' THEN v.vote_id END) AS reproduce_count,
    COUNT(DISTINCT c.comment_id) AS comment_count,
    cp.created_at
FROM community_posts cp
LEFT JOIN students s ON cp.student_id = s.student_id
LEFT JOIN users u ON s.user_id = u.user_id
LEFT JOIN post_votes v ON cp.post_id = v.post_id
LEFT JOIN comments c ON cp.post_id = c.post_id
GROUP BY cp.post_id
ORDER BY cp.created_at DESC;

-- Query 2: Bài viết có nhiều người "reproduce error"
SELECT 
    p.post_id,
    p.title,
    COUNT(pv.vote_id) AS reproduce_count
FROM community_posts p
JOIN post_votes pv ON p.post_id = pv.post_id
WHERE pv.vote_type = 'REPRODUCE_ERROR'
GROUP BY p.post_id
ORDER BY reproduce_count DESC
LIMIT 10;

SELECT 'Community Correct Schema created! ✅' AS status;
SELECT 'Vote types: UPVOTE_FIX và REPRODUCE_ERROR (khớp với Android code)' AS note;
