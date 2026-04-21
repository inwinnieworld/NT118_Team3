-- ============================================
-- GIT COMMIT JOURNAL - SEED DATA
-- ============================================

-- ============================================
-- MASTER DATA: EMOTIONS (15 cảm xúc)
-- ============================================
-- Phân loại:
-- NEGATIVE (7): Ác Quỷ, Buồn Một Chút, Buồn Nhiều Chút, Hối Lỗi, Hơi Quạo, Khinh Bỉ
-- POSITIVE (6): Chúa Hề, Háo Hức, LMAO, Thiên Thần, Vui Vẻ, Yêu Thương
-- NEUTRAL (3): Buồn Ngủ, Suy Ngẫm, Ý Kiến
-- 
-- base_weight: Trọng số ảnh hưởng (1=nhẹ, 2=trung bình, 3=nặng)
-- ============================================

INSERT INTO emotions (emotion_name, emotion_category, base_weight, icon_url, color_hex) VALUES
-- NEGATIVE EMOTIONS (Cảm xúc tiêu cực)
('Ác Quỷ', 'NEGATIVE', 3, 'icon_acquy.png', '#DC2626'),           -- Giận dữ, căm phẫn cực độ
('Buồn Một Chút', 'NEGATIVE', 1, 'icon_buonmotchut.png', '#9CA3AF'), -- Buồn nhẹ, hơi thất vọng
('Buồn Nhiều Chút', 'NEGATIVE', 2, 'icon_buonnhieuchut.png', '#6B7280'), -- Buồn nhiều, chán nản
('Hối Lỗi', 'NEGATIVE', 2, 'icon_hoiloi.png', '#8B5CF6'),         -- Hối hận, tự trách
('Hơi Quạo', 'NEGATIVE', 2, 'icon_hoiquao.png', '#EF4444'),       -- Bực mình, khó chịu
('Khinh Bỉ', 'NEGATIVE', 3, 'icon_khinhbi.png', '#B91C1C'),       -- Khinh thường, ghê tởm

-- POSITIVE EMOTIONS (Cảm xúc tích cực)
('Chúa Hề', 'POSITIVE', 2, 'icon_chuahe.png', '#F59E0B'),         -- Vui nhộn, hài hước
('Háo Hức', 'POSITIVE', 3, 'icon_haohuc.png', '#10B981'),         -- Phấn khích, hào hứng
('LMAO', 'POSITIVE', 3, 'icon_lmao.png', '#FBBF24'),              -- Cười sảng khoái
('Thiên Thần', 'POSITIVE', 3, 'icon_thienthan.png', '#EC4899'),   -- Tốt bụng, nhân hậu
('Vui Vẻ', 'POSITIVE', 3, 'icon_vuive.png', '#22C55E'),           -- Vui vẻ, hạnh phúc
('Yêu Thương', 'POSITIVE', 3, 'icon_yeuthuong.png', '#F472B6'),   -- Yêu thương, trân trọng

-- NEUTRAL EMOTIONS (Cảm xúc trung tính)
('Buồn Ngủ', 'NEUTRAL', 1, 'icon_buonngu.png', '#94A3B8'),        -- Mệt mỏi, buồn ngủ
('Suy Ngẫm', 'NEUTRAL', 1, 'icon_suyngam.png', '#6366F1'),        -- Suy nghĩ, trầm tư
('Ý Kiến', 'NEUTRAL', 1, 'icon_ykien.png', '#3B82F6');            -- Có ý kiến, quan điểm

-- ============================================
-- SAMPLE DATA: COMMITS (3 ngày mẫu)
-- ============================================
-- student_id = 1 (Test user)
-- Mô phỏng 3 ngày với các tình huống thực tế
-- Bao gồm cả MAIN BRANCH và QUEST BRANCH để test Git Graph
-- ============================================

-- ========== NGÀY 1: 2026-04-19 (Thứ Bảy - Ngày tích cực) ==========
-- Chỉ có Main Branch
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 11, 'main', NULL, 80, 'Hoàn thành project đúng deadline!', '2026-04-19 09:30:00'),
(1, 8, 'main', NULL, 75, 'Team work rất hiệu quả', '2026-04-19 11:45:00'),
(1, 13, 'main', NULL, 85, 'Code chạy ngon lành', '2026-04-19 14:20:00'),
(1, 9, 'main', NULL, 90, 'Demo thành công trước khách hàng', '2026-04-19 16:00:00'),
(1, 12, 'main', NULL, 70, 'Được team khen ngợi', '2026-04-19 18:30:00');

-- ========== NGÀY 2: 2026-04-20 (Chủ Nhật - Ngày có Quest Branch) ==========
-- Main Branch: Phát hiện bug nghiêm trọng
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 1, 'main', NULL, 85, 'Bug nghiêm trọng phát sinh sau demo', '2026-04-20 08:00:00'),
(1, 5, 'main', NULL, 70, 'Khách hàng phàn nàn nhiều', '2026-04-20 10:30:00');

-- Quest Branch: Tạo quest để fix bug (user_quest_id = 1)
-- Quest bắt đầu từ 11:00, kết thúc 17:30
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 4, 'quest', 1, 75, '[Quest #1] Buồn vì phải debug cả ngày', '2026-04-20 11:00:00'),
(1, 7, 'quest', 1, 65, '[Quest #1] Hối hận vì không test kỹ', '2026-04-20 13:15:00'),
(1, 14, 'quest', 1, 60, '[Quest #1] Suy nghĩ về cách fix', '2026-04-20 14:30:00'),
(1, 8, 'quest', 1, 70, '[Quest #1] Háo hức với giải pháp mới', '2026-04-20 15:45:00'),
(1, 13, 'quest', 1, 80, '[Quest #1] Fix được bug chính!', '2026-04-20 17:30:00');

-- Main Branch: Sau khi hoàn thành quest (merge point)
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 10, 'main', NULL, 85, 'LMAO bug đã được fix!', '2026-04-20 18:00:00'),
(1, 12, 'main', NULL, 75, 'Cảm ơn bản thân đã kiên trì', '2026-04-20 19:00:00');

-- ========== NGÀY 3: 2026-04-21 (Thứ Hai - Ngày có 2 Quest Branches) ==========
-- Main Branch: Bắt đầu ngày mới
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 13, 'main', NULL, 70, 'Bắt đầu ngày mới với tinh thần tốt', '2026-04-21 08:30:00');

-- Quest Branch #2: Refactor code (user_quest_id = 2)
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 2, 'quest', 2, 45, '[Quest #2] Hơi buồn vì phải refactor', '2026-04-21 09:00:00'),
(1, 14, 'quest', 2, 55, '[Quest #2] Suy nghĩ về architecture', '2026-04-21 10:30:00'),
(1, 13, 'quest', 2, 75, '[Quest #2] Refactor xong, code sạch hơn', '2026-04-21 12:00:00');

-- Main Branch: Giữa ngày
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 15, 'main', NULL, 60, 'Có ý kiến về code review', '2026-04-21 13:00:00');

-- Quest Branch #3: Optimize performance (user_quest_id = 3)
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 8, 'quest', 3, 70, '[Quest #3] Háo hức optimize performance', '2026-04-21 14:00:00'),
(1, 10, 'quest', 3, 85, '[Quest #3] LMAO tăng tốc 50%!', '2026-04-21 16:00:00');

-- Main Branch: Kết thúc ngày
INSERT INTO commits (student_id, emotion_id, branch_type, user_quest_id, intensity_level, message, created_at) VALUES
(1, 12, 'main', NULL, 80, 'Ngày làm việc hiệu quả', '2026-04-21 18:00:00');

-- ============================================
-- SAMPLE DATA: DAILY MERGES
-- ============================================
-- Daily merge tổng hợp TẤT CẢ commits (main + quest) trong ngày
-- Chỉ merge cho các ngày ĐÃ QUA (19/04 và 20/04)
-- Ngày 21/04 CHƯA merge vì đang trong ngày
-- ============================================

-- Merge cho Ngày 1 (2026-04-19) - Chỉ có Main Branch
INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged) VALUES
(1, '2026-04-19', 9,
'{"LMAO": {"emotion_id": 9, "emotion_name": "LMAO", "emotion_category": "POSITIVE", "color_hex": "#FBBF24", "frequency": 0.2, "avg_intensity": 90, "impact_score": 18.0}, "Vui Vẻ": {"emotion_id": 13, "emotion_name": "Vui Vẻ", "emotion_category": "POSITIVE", "color_hex": "#22C55E", "frequency": 0.2, "avg_intensity": 85, "impact_score": 17.0}, "Háo Hức": {"emotion_id": 8, "emotion_name": "Háo Hức", "emotion_category": "POSITIVE", "color_hex": "#10B981", "frequency": 0.2, "avg_intensity": 75, "impact_score": 15.0}, "Vui Vẻ": {"emotion_id": 11, "emotion_name": "Vui Vẻ", "emotion_category": "POSITIVE", "color_hex": "#22C55E", "frequency": 0.2, "avg_intensity": 80, "impact_score": 16.0}, "Yêu Thương": {"emotion_id": 12, "emotion_name": "Yêu Thương", "emotion_category": "POSITIVE", "color_hex": "#F472B6", "frequency": 0.2, "avg_intensity": 70, "impact_score": 14.0}}',
'Ngày tuyệt vời! Project thành công và team work rất tốt.',
FALSE);

-- Merge cho Ngày 2 (2026-04-20) - Có Main + Quest Branch
-- Tổng: 2 main + 5 quest + 2 main = 9 commits
INSERT INTO daily_merges (student_id, merge_date, dominant_emotion_id, emotion_stats, user_retrospective, is_auto_merged) VALUES
(1, '2026-04-20', 1,
'{"Ác Quỷ": {"emotion_id": 1, "emotion_name": "Ác Quỷ", "emotion_category": "NEGATIVE", "color_hex": "#DC2626", "frequency": 0.111, "avg_intensity": 85, "impact_score": 9.4}, "Hơi Quạo": {"emotion_id": 5, "emotion_name": "Hơi Quạo", "emotion_category": "NEGATIVE", "color_hex": "#EF4444", "frequency": 0.111, "avg_intensity": 70, "impact_score": 7.8}, "Buồn Nhiều Chút": {"emotion_id": 4, "emotion_name": "Buồn Nhiều Chút", "emotion_category": "NEGATIVE", "color_hex": "#6B7280", "frequency": 0.111, "avg_intensity": 75, "impact_score": 8.3}, "Hối Lỗi": {"emotion_id": 7, "emotion_name": "Hối Lỗi", "emotion_category": "NEGATIVE", "color_hex": "#8B5CF6", "frequency": 0.111, "avg_intensity": 65, "impact_score": 7.2}, "Suy Ngẫm": {"emotion_id": 14, "emotion_name": "Suy Ngẫm", "emotion_category": "NEUTRAL", "color_hex": "#6366F1", "frequency": 0.111, "avg_intensity": 60, "impact_score": 6.7}, "Háo Hức": {"emotion_id": 8, "emotion_name": "Háo Hức", "emotion_category": "POSITIVE", "color_hex": "#10B981", "frequency": 0.111, "avg_intensity": 70, "impact_score": 7.8}, "Vui Vẻ": {"emotion_id": 13, "emotion_name": "Vui Vẻ", "emotion_category": "POSITIVE", "color_hex": "#22C55E", "frequency": 0.111, "avg_intensity": 80, "impact_score": 8.9}, "LMAO": {"emotion_id": 10, "emotion_name": "LMAO", "emotion_category": "POSITIVE", "color_hex": "#FBBF24", "frequency": 0.111, "avg_intensity": 85, "impact_score": 9.4}, "Yêu Thương": {"emotion_id": 12, "emotion_name": "Yêu Thương", "emotion_category": "POSITIVE", "color_hex": "#F472B6", "frequency": 0.111, "avg_intensity": 75, "impact_score": 8.3}}',
'Ngày đầy thử thách! Gặp bug nghiêm trọng nhưng đã fix thành công nhờ quest. Học được bài học về testing.',
FALSE);

-- NOTE: Ngày 21/04 CHƯA có merge vì đang trong ngày (chưa đến 22:00)
-- User có thể merge thủ công từ 22:00-23:59 hoặc hệ thống tự động merge lúc 00:00 ngày 22/04

-- ============================================
-- SAMPLE DATA: SEVERITY ALERTS
-- ============================================

-- Alert cho Ngày 2 (2026-04-20) - High severity detected
INSERT INTO severity_alerts (student_id, branch_type, alert_type, severity_score, alert_message, triggered_at) VALUES
(1, 'main', 'HIGH_SEVERITY', 75.00, 'Phát hiện mức độ tiêu cực cao trong 3 ngày qua (75.00%). Khuyến nghị sử dụng chức năng Error Logs để cải thiện tâm trạng.', '2026-04-20 18:00:00');
