package com.example.emotiondebugging.utils;

import android.os.Build;

/**
 * API Configuration - Quản lý BASE_URL tập trung
 *
 * BASE_URL được TỰ ĐỘNG chọn theo môi trường chạy (không cần đổi cờ tay):
 * - Emulator  → http://10.0.2.2:{PORT}   (10.0.2.2 = localhost của máy tính)
 * - Máy thật  → http://{IP_ADDRESS}:{PORT}
 *
 * ⚠️ CHỈ cần cập nhật IP_ADDRESS cho khớp IPv4 của máy chạy backend khi dùng máy thật
 * (máy thật + backend phải chung mạng WiFi). Xem IP bằng: ipconfig (Windows) / ifconfig.
 */
public class ApiConstants {

    // ==================== CẤU HÌNH ====================

    /**
     * Địa chỉ IP LAN của máy tính chạy backend (chỉ dùng khi chạy trên máy thật).
     * Kiểm tra bằng: ipconfig (Windows) hoặc ifconfig (Mac/Linux) → dòng IPv4 Address.
     */
    private static final String IP_ADDRESS = "192.168.31.192";

    /**
     * Port của backend server
     */
    private static final String PORT = "3000";

    // ==================== BASE URL ====================

    /**
     * BASE_URL tự động: emulator dùng 10.0.2.2, máy thật dùng IP LAN.
     * Có dấu "/" cuối để Retrofit ghép path đúng chuẩn.
     */
    public static final String BASE_URL = isEmulator()
            ? "http://10.0.2.2:" + PORT + "/"
            : "http://" + IP_ADDRESS + ":" + PORT + "/";

    /**
     * Nhận diện đang chạy trên Android Emulator hay máy thật (dựa trên Build fingerprint).
     * Bao trùm các emulator phổ biến: AVD chính chủ, Genymotion, BlueStacks...
     */
    private static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.FINGERPRINT.contains("emulator")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for")
                || Build.MODEL.contains("google_sdk")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic")
                || Build.DEVICE.startsWith("generic")
                || Build.PRODUCT.contains("sdk")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu");
    }
    
    /**
     * Get full URL for avatar/uploads
     * @param relativePath: /uploads/avatar-xxx.jpg
     * @return Full URL: http://100.87.39.55:3000/uploads/avatar-xxx.jpg
     */
    public static String getFullUrl(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()) {
            return "";
        }
        // BASE_URL đã có "/" cuối → bỏ "/" đầu của relativePath để tránh double slash.
        return BASE_URL + (relativePath.startsWith("/") ? relativePath.substring(1) : relativePath);
    }
}
