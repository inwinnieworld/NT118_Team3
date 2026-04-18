package com.example.emotiondebugging.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {

    private static final String PREF_NAME = "EmotionDebuggingPrefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_STUDENT_CODE = "student_code";
    private static final String KEY_ROLE = "role";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_EXPIRE_TIME = "expire_time";
    private static final String KEY_NAME = "user_name";

    private final SharedPreferences prefs;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Lưu token và set thời gian hết hạn
    public void saveToken(String token) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        // Token hết hạn sau 30 giây để test
        long durationInMillis = 7L * 24 * 60 * 60 * 1000;
        long expireTime = System.currentTimeMillis() + durationInMillis;
        editor.putLong(KEY_EXPIRE_TIME, expireTime);
        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // Lưu toàn bộ thông tin user
    public void saveUserInfo(String userId, String email, String studentCode, String role, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_STUDENT_CODE, studentCode);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NAME, name);
        editor.apply();
    }

    // ===== User ID =====
    public void saveUserId(String userId) {
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    // ===== Email =====
    public void saveEmail(String email) {
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    // ===== Student code =====
    public void saveStudentCode(String studentCode) {
        prefs.edit().putString(KEY_STUDENT_CODE, studentCode).apply();
    }

    public String getStudentCode() {
        return prefs.getString(KEY_STUDENT_CODE, null);
    }

    // ===== Role =====
    public void saveRole(String role) {
        prefs.edit().putString(KEY_ROLE, role).apply();
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "STUDENT");
    }

    // Wrapper để code mới dùng
    public void saveUserRole(String role) {
        saveRole(role);
    }

    public String getUserRole() {
        return getRole();
    }

    // ===== Name =====
    public void saveName(String name) {
        prefs.edit().putString(KEY_NAME, name).apply();
    }

    public String getName() {
        return prefs.getString(KEY_NAME, "Người dùng");
    }

    // Wrapper để StaffDashboardActivity gọi được
    public void saveUserName(String userName) {
        saveName(userName);
    }

    public String getUserName() {
        return getName();
    }

    // Kiểm tra đăng nhập và token còn hạn
    public boolean isLoggedIn() {
        boolean hasLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);
        if (!hasLoggedIn) return false;

        long expireTime = prefs.getLong(KEY_EXPIRE_TIME, 0);
        if (System.currentTimeMillis() > expireTime) {
            logout();
            return false;
        }
        return true;
    }

    // Xóa tất cả dữ liệu
    public void clearAll() {
        prefs.edit().clear().apply();
    }

    // Logout: xóa token và thông tin user
    public void logout() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_NAME);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_EMAIL);
        editor.remove(KEY_STUDENT_CODE);
        editor.remove(KEY_ROLE);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}