-- ============================================
-- Script: Thêm cột emergency_phone vào bảng STUDENTS
-- Mục đích: Fix database để chạy test Profile Activity
-- Ngày: 14/04/2026
-- ============================================

USE emotion_debugging;

-- Bước 1: Thêm cột emergency_phone
ALTER TABLE STUDENTS 
ADD COLUMN emergency_phone VARCHAR(15) AFTER year_of_study;

-- Bước 2: Update dữ liệu mẫu cho 20 students
UPDATE STUDENTS SET emergency_phone = '0909999001' WHERE student_id = 1;
UPDATE STUDENTS SET emergency_phone = '0909999002' WHERE student_id = 2;
UPDATE STUDENTS SET emergency_phone = '0909999003' WHERE student_id = 3;
UPDATE STUDENTS SET emergency_phone = '0909999004' WHERE student_id = 4;
UPDATE STUDENTS SET emergency_phone = '0909999005' WHERE student_id = 5;
UPDATE STUDENTS SET emergency_phone = '0909999006' WHERE student_id = 6;
UPDATE STUDENTS SET emergency_phone = '0909999007' WHERE student_id = 7;
UPDATE STUDENTS SET emergency_phone = '0909999008' WHERE student_id = 8;
UPDATE STUDENTS SET emergency_phone = '0909999009' WHERE student_id = 9;
UPDATE STUDENTS SET emergency_phone = '0909999010' WHERE student_id = 10;
UPDATE STUDENTS SET emergency_phone = '0909999011' WHERE student_id = 11;
UPDATE STUDENTS SET emergency_phone = '0909999012' WHERE student_id = 12;
UPDATE STUDENTS SET emergency_phone = '0909999013' WHERE student_id = 13;
UPDATE STUDENTS SET emergency_phone = '0909999014' WHERE student_id = 14;
UPDATE STUDENTS SET emergency_phone = '0909999015' WHERE student_id = 15;
UPDATE STUDENTS SET emergency_phone = '0909999016' WHERE student_id = 16;
UPDATE STUDENTS SET emergency_phone = '0909999017' WHERE student_id = 17;
UPDATE STUDENTS SET emergency_phone = '0909999018' WHERE student_id = 18;
UPDATE STUDENTS SET emergency_phone = '0909999019' WHERE student_id = 19;
UPDATE STUDENTS SET emergency_phone = '0909999020' WHERE student_id = 20;

-- Bước 3: Verify kết quả
SELECT 
    u.user_id, 
    u.name, 
    u.email, 
    s.student_code, 
    s.emergency_phone 
FROM USERS u 
JOIN STUDENTS s ON u.user_id = s.user_id 
WHERE u.user_id = 1;

-- Expected output:
-- user_id | name           | email              | student_code | emergency_phone
-- 1       | Nguyễn Văn An  | an.nv@gmail.com    | SV001        | 0909999001

SELECT '✅ Database updated successfully!' AS status;
