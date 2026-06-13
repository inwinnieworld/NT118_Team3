-- =====================================================================================
-- THÊM CÁC BẢNG EMERGENCY ĐANG THIẾU
-- =====================================================================================

USE emotion_debugging;

-- Bảng emergency_contacts: Danh bạ liên hệ khẩn cấp của sinh viên
CREATE TABLE IF NOT EXISTS emergency_contacts (
    contact_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    contact_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    relationship VARCHAR(50) COMMENT 'Cha, Mẹ, Anh/Chị, Bạn bè, Khác',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (student_id) REFERENCES students(student_id) ON DELETE CASCADE,
    INDEX idx_student (student_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Bảng emergency_resources: Các tổ chức hỗ trợ khẩn cấp
CREATE TABLE IF NOT EXISTS emergency_resources (
    resource_id INT AUTO_INCREMENT PRIMARY KEY,
    resource_name VARCHAR(200) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    sms_phone VARCHAR(20) COMMENT 'Số điện thoại nhận SMS (nếu có)',
    resource_type ENUM('MENTAL_HEALTH', 'MEDICAL', 'POLICE', 'OTHER') NOT NULL,
    description TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_resource_type (resource_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed data mẫu cho emergency_resources
INSERT INTO emergency_resources (resource_name, phone, sms_phone, resource_type, description) VALUES
('Đường dây nóng tâm lý - UIT', '18001567', '0901234567', 'MENTAL_HEALTH', 'Hỗ trợ tâm lý cho sinh viên UIT, hoạt động 24/7'),
('Tổng đài tư vấn tâm lý Quốc gia', '1800558858', NULL, 'MENTAL_HEALTH', 'Tư vấn tâm lý miễn phí cho mọi người'),
('Bệnh viện Chợ Rẫy', '0283855418', NULL, 'MEDICAL', 'Bệnh viện đa khoa lớn nhất TP.HCM'),
('Bệnh viện Nhi Đồng 1', '0283829034', NULL, 'MEDICAL', 'Bệnh viện chuyên khoa nhi'),
('Cảnh sát 113', '113', NULL, 'POLICE', 'Số điện thoại khẩn cấp cảnh sát'),
('Cứu hỏa 114', '114', NULL, 'OTHER', 'Số điện thoại khẩn cấp cứu hỏa'),
('Cấp cứu 115', '115', NULL, 'MEDICAL', 'Số điện thoại cấp cứu y tế');

SELECT 'Emergency tables added successfully!' AS Status;
