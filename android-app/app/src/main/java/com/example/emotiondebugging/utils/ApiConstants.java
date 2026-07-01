package com.example.emotiondebugging.utils;

/**
 * API Configuration - Quản lý BASE_URL tập trung
 * 
 * HƯỚNG DẪN SỬ DỤNG:
 * - Chạy trên Emulator: Đổi IS_EMULATOR = true
 * - Chạy trên điện thoại thật: Đổi IS_EMULATOR = false (và cập nhật IP_ADDRESS nếu cần)
 */
public class ApiConstants {

    // ==================== CẤU HÌNH ====================
    
    /**
     * ⚠️ THAY ĐỔI Ở ĐÂY:
     * - true: Chạy trên Android Emulator
     * - false: Chạy trên điện thoại thật (Real Device)
     */
    private static final boolean IS_EMULATOR = false;
    
    /**
     * Địa chỉ IP của máy tính (chạy backend)
     * Kiểm tra bằng lệnh: ipconfig (Windows) hoặc ifconfig (Mac/Linux)
     * Tìm dòng IPv4 Address
     */
    private static final String IP_ADDRESS = "192.168.31.192";
    
    /**
     * Port của backend server
     */
    private static final String PORT = "3000";
    
    // ==================== BASE URL ====================
    
    /**
     * BASE_URL được tự động chọn dựa trên IS_EMULATOR
     * - Emulator: http://10.0.2.2:3000 (localhost mapping)
     * - Real Device: http://{IP_ADDRESS}:3000
     */
    public static final String BASE_URL = IS_EMULATOR 
            ? "http://10.0.2.2:" + PORT
            : "http://" + IP_ADDRESS + ":" + PORT;
    
    /**
     * Get full URL for avatar/uploads
     * @param relativePath: /uploads/avatar-xxx.jpg
     * @return Full URL: http://100.87.39.55:3000/uploads/avatar-xxx.jpg
     */
    public static String getFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        // Đảm bảo không bị duplicate slash
        return BASE_URL + (relativePath.startsWith("/") ? relativePath : "/" + relativePath);
    }
}
