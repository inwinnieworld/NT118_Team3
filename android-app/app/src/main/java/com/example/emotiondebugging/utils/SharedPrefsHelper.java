package com.example.emotiondebugging.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ✅ Helper class để lưu trữ dữ liệu persistent (token, user info)
 * Sử dụng SharedPreferences - key-value storage của Android
 */
public class SharedPrefsHelper {

    // Tên của file lưu trữ trong hệ thống Android
    private static final String PREF_NAME = "EmotionDebuggingPrefs";

    // Các "chìa khóa" (keys) để cất và lấy dữ liệu
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_STUDENT_CODE = "student_code";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    /**
     * ✅ Constructor - Khởi tạo SharedPreferences
     * @param context Application context
     */
    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    /**
     * ✅ Lưu token sau khi login thành công
     * @param token JWT token từ server
     */
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.apply(); // Dùng apply() thay vì commit() để chạy bất đồng bộ (nhanh hơn)
    }

    /**
     * ✅ Lấy token đã lưu
     * @return Token string hoặc null nếu chưa login
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * ✅ Lưu thông tin cơ bản của User
     */
    public void saveUserInfo(String userId, String email, String studentCode, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_STUDENT_CODE, studentCode);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    /**
     * ✅ Lấy User ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * ✅ Lấy Email
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * ✅ Lấy Mã Sinh Viên
     */
    public String getStudentCode() {
        return prefs.getString(KEY_STUDENT_CODE, null);
    }

    /**
     * ✅ Lấy Role (Quan trọng cho việc phân luồng màn hình chính)
     * Mặc định nếu không thấy sẽ trả về "STUDENT" để an toàn
     */
    public String getRole() {
        return prefs.getString(KEY_ROLE, "STUDENT");
    }

    /**
     * ✅ Kiểm tra user đã login chưa
     * @return true nếu đã login, false nếu chưa
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * ✅ Xóa tất cả dữ liệu (Dùng khi Logout)
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    /**
     * ✅ Tùy chọn Logout nhẹ: Chỉ xóa cờ đăng nhập và token, giữ lại email để lần sau gõ cho nhanh
     */
    public void logout() {
        editor.remove(KEY_TOKEN);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}