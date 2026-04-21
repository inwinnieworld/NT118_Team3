-- =====================================================================================
-- MODULE: HỆ THỐNG COMMUNITY (Mạng xã hội & Thảo luận về Debugging)
-- Vote Types: UPVOTE (hữu ích) và DOWNVOTE (không hữu ích/spam)
-- =====================================================================================

-- 1. BẢNG BÀI VIẾT CỘNG ĐỒNG
-- Sinh viên chia sẻ vấn đề, lỗi, hoặc thảo luận về debugging
CREATE TABLE community_posts (
    post_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    error_type_id INT DEFAULT NULL,           -- Liên kết với ERROR_TYPES (optional)
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT TRUE,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (error_type_id) REFERENCES ERROR_TYPES(error_type_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. BẢNG BÌNH LUẬN
-- Hỗ trợ nested comments (reply to comment)
CREATE TABLE comments (
    comment_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    parent_comment_id INT DEFAULT NULL,       -- NULL = comment bài viết, có giá trị = reply
    content TEXT NOT NULL,
    is_anonymous BOOLEAN DEFAULT FALSE,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. BẢNG TƯƠNG TÁC BÀI VIẾT (Votes)
-- Vote types: UPVOTE (hữu ích) và DOWNVOTE (không hữu ích/spam)
CREATE TABLE post_votes (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    post_id INT NOT NULL,
    student_id INT NOT NULL,
    vote_type ENUM('UPVOTE', 'DOWNVOTE') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_post_vote (post_id, student_id),
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. BẢNG TƯƠNG TÁC BÌNH LUẬN
-- Vote types giống như post votes
CREATE TABLE comment_votes (
    vote_id INT AUTO_INCREMENT PRIMARY KEY,
    comment_id INT NOT NULL,
    student_id INT NOT NULL,
    vote_type ENUM('UPVOTE', 'DOWNVOTE') NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_comment_vote (comment_id, student_id),
    FOREIGN KEY (comment_id) REFERENCES comments(comment_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. BẢNG LƯU BÀI VIẾT (Saved Posts)
-- Sinh viên lưu bài viết để đọc lại sau
CREATE TABLE saved_posts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    post_id INT NOT NULL,
    saved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_saved (student_id, post_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES community_posts(post_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. BẢNG CHẶN/ẨN NGƯỜI DÙNG (Muted Authors)
-- Sinh viên có thể ẩn bài viết từ tác giả cụ thể
CREATE TABLE muted_authors (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,                  -- Người thực hiện mute
    muted_student_id INT NOT NULL,            -- Người bị mute
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_muted (student_id, muted_student_id),
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    FOREIGN KEY (muted_student_id) REFERENCES students(student_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================================
-- SEED DATA - Dữ liệu mẫu để test
-- =====================================================================================

-- Giả sử đã có students với student_id: 1, 2, 3
-- Giả sử đã có ERROR_TYPES với error_type_id: 1 (Syntax Error), 2 (Logic Error)

-- Thêm bài viết mẫu
INSERT INTO community_posts (student_id, error_type_id, title, content, is_anonymous, view_count) VALUES
(1, 1, 'Lỗi NullPointerException khi gọi API', 'Mình đang gặp lỗi NullPointerException khi parse JSON response từ API. Đã thử nhiều cách nhưng vẫn không được. Có ai gặp tương tự không?', FALSE, 45),
(2, 2, 'RecyclerView không hiển thị dữ liệu', 'RecyclerView của mình không hiển thị gì cả dù đã set adapter và data. Debug thì thấy data có nhưng UI không render. Help!', TRUE, 32),
(3, 1, 'Gradle build failed: Duplicate class', 'Build project bị lỗi duplicate class. Mình nghĩ là conflict dependencies nhưng không biết fix thế nào.', FALSE, 28),
(1, NULL, 'Tips: Cách debug hiệu quả với Logcat', 'Chia sẻ một số tips debug với Logcat mà mình hay dùng: 1) Dùng filter theo tag, 2) Tìm kiếm theo keyword, 3) Export log ra file...', FALSE, 120);

-- Thêm votes cho bài viết
INSERT INTO post_votes (post_id, student_id, vote_type) VALUES
(1, 2, 'UPVOTE'),              -- Student 2 thấy bài viết 1 hữu ích
(1, 3, 'UPVOTE'),              -- Student 3 cũng thấy hữu ích
(2, 1, 'UPVOTE'),              -- Student 1 thấy bài viết 2 hữu ích
(2, 3, 'DOWNVOTE'),            -- Student 3 thấy bài viết 2 không hữu ích
(3, 1, 'UPVOTE'),              -- Student 1 thấy bài viết 3 hữu ích
(3, 2, 'DOWNVOTE'),            -- Student 2 thấy bài viết 3 không hữu ích (spam)
(4, 2, 'UPVOTE'),              -- Student 2 thấy tips debug hữu ích
(4, 3, 'UPVOTE');              -- Student 3 cũng thấy hữu ích

-- Thêm comments
INSERT INTO comments (post_id, student_id, content, is_anonymous) VALUES
(1, 2, 'Bạn đã thử kiểm tra response có null không? Nên dùng Optional hoặc null check trước khi parse.', FALSE),
(1, 3, 'Mình cũng gặp lỗi này, fix bằng cách thêm @Nullable annotation và check null trước khi dùng.', FALSE),
(2, 1, 'Bạn đã gọi notifyDataSetChanged() sau khi set data chưa?', TRUE),
(3, 2, 'Check file build.gradle xem có dependency nào bị duplicate version không. Thử exclude module conflict.', FALSE),
(4, 2, 'Cảm ơn bạn! Mình không biết có thể export log ra file. Rất hữu ích!', FALSE);

-- Thêm reply (nested comment)
INSERT INTO comments (post_id, student_id, parent_comment_id, content, is_anonymous) VALUES
(1, 1, 1, 'Cảm ơn bạn! Mình đã thử và nó work rồi. Vấn đề là mình quên check null trước khi parse.', FALSE),
(2, 2, 3, 'Rồi bạn, mình đã gọi rồi nhưng vẫn không hiển thị. Có thể do adapter setup sai.', TRUE);

-- Thêm comment votes
INSERT INTO comment_votes (comment_id, student_id, vote_type) VALUES
(1, 1, 'UPVOTE'),              -- Student 1 thấy comment 1 hữu ích
(1, 3, 'UPVOTE'),              -- Student 3 cũng thấy hữu ích
(2, 1, 'UPVOTE'),              -- Student 1 thấy comment 2 hữu ích
(3, 2, 'UPVOTE'),              -- Student 2 thấy comment 3 hữu ích
(4, 1, 'DOWNVOTE');            -- Student 1 thấy comment 4 không hữu ích

-- Thêm saved posts
INSERT INTO saved_posts (student_id, post_id) VALUES
(1, 2),  -- Student 1 lưu bài viết 2
(1, 4),  -- Student 1 lưu bài viết 4 (tips debug)
(2, 1),  -- Student 2 lưu bài viết 1
(3, 1),  -- Student 3 lưu bài viết 1
(3, 4);  -- Student 3 lưu bài viết 4

-- Thêm muted authors (optional - để test)
-- INSERT INTO muted_authors (student_id, muted_student_id) VALUES
-- (1, 3);  -- Student 1 mute Student 3

-- =====================================================================================
-- INDEXES để tối ưu performance
-- =====================================================================================

CREATE INDEX idx_posts_created_at ON community_posts(created_at DESC);
CREATE INDEX idx_posts_error_type ON community_posts(error_type_id);
CREATE INDEX idx_posts_student ON community_posts(student_id);
CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_parent ON comments(parent_comment_id);
CREATE INDEX idx_post_votes_post ON post_votes(post_id);
CREATE INDEX idx_comment_votes_comment ON comment_votes(comment_id);

-- =====================================================================================
-- NOTES
-- =====================================================================================
-- 1. Vote Types (Đơn giản hóa):
--    - UPVOTE: Bài viết/comment hữu ích, có giá trị
--    - DOWNVOTE: Không hữu ích, spam, sai thông tin
--
-- 2. Tính điểm bài viết:
--    - Score = upvote_count - downvote_count
--    - Bài viết có score cao sẽ hiển thị trên đầu (filter: best)
--
-- 3. Backend controller cần cập nhật:
--    - Đổi 'UPVOTE_FIX' thành 'UPVOTE'
--    - Đổi 'REPRODUCE_ERROR' thành 'DOWNVOTE'
--    - Cập nhật logic đếm votes
--
-- 4. Android code cần cập nhật:
--    - PostDetailActivity.java: đổi vote_type thành "UPVOTE" và "DOWNVOTE"
--    - CommunityActivity.java: đổi vote_type thành "UPVOTE" và "DOWNVOTE"
--    - CommentAdapter.java: đổi vote_type thành "UPVOTE" và "DOWNVOTE"
