package com.example.emotiondebugging.utils;

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
    // private static final String IP_ADDRESS = "192.168.31.192";
    // private static final String IP_ADDRESS = "192.168.1.197";
    private static final String IP_ADDRESS = "192.168.1.65";
    /**
     * Port của backend server
     */
    private static final String PORT = "3000";

    // ==================== BASE URL ====================

    /**
     * BASE_URL: cả emulator lẫn máy thật đều dùng IP LAN của máy chạy backend.
     * (Trước đây emulator dùng 10.0.2.2 nhưng trên Windows hay bị Firewall chặn
     *  loopback ảo → "Failed to connect". IP LAN thì emulator gọi được qua NAT,
     *  giống hệt máy thật, nên ổn định hơn.)
     * Có dấu "/" cuối để Retrofit ghép path đúng chuẩn.
     */
    public static final String BASE_URL = "http://" + IP_ADDRESS + ":" + PORT + "/";

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
