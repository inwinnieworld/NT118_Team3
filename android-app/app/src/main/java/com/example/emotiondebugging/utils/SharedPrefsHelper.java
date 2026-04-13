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
    private final SharedPreferences.Editor editor;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // ✅ saveToken: chỉ lo việc của token, không đụng đến name
    public void saveToken(String token) {
        editor.putString(KEY_TOKEN, token);
        editor.putBoolean(KEY_IS_LOGGED_IN, true);

        long durationInMillis = 60 * 60 * 1000;
        long expireTime = System.currentTimeMillis() + durationInMillis;
        editor.putLong(KEY_EXPIRE_TIME, expireTime);

        editor.apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    // ✅ saveUserInfo: thêm name vào đây — đúng trách nhiệm, đủ thông tin user
    public void saveUserInfo(String userId, String email, String studentCode, String role, String name) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_STUDENT_CODE, studentCode);
        editor.putString(KEY_ROLE, role);
        editor.putString(KEY_NAME, name); // ✅ name thuộc về đây
        editor.apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    public String getStudentCode() {
        return prefs.getString(KEY_STUDENT_CODE, null);
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "STUDENT");
    }

    // ✅ getName: fallback "Người dùng" chỉ là safety net thực sự, không phải giá trị mặc định
    public String getName() {
        return prefs.getString(KEY_NAME, "Người dùng");
    }

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

    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    public void logout() {
        editor.remove(KEY_TOKEN);
        editor.remove(KEY_NAME); // ✅ Xóa name khi logout, tránh hiển thị tên user cũ
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}