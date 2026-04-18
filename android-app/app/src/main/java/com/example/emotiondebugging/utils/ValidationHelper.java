package com.example.emotiondebugging.utils;

import android.text.TextUtils;
import android.util.Patterns;

import java.util.regex.Pattern;

public final class ValidationHelper {

    private ValidationHelper() {
    }

    private static final Pattern LOGIN_ACCOUNT_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]{3,30}$");

    public static String validateLoginAccount(String account) {
        if (TextUtils.isEmpty(account)) {
            return "Tài khoản không được để trống";
        }

        String value = account.trim();

        // Chấp nhận email
        if (Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            return null;
        }

        // Hoặc username / mã sinh viên / account
        if (LOGIN_ACCOUNT_PATTERN.matcher(value).matches()) {
            return null;
        }

        return "Tài khoản phải là email hoặc tên đăng nhập hợp lệ";
    }

    public static String validateLoginPassword(String password) {
        if (TextUtils.isEmpty(password)) {
            return "Mật khẩu không được để trống";
        }

        return null;
    }
}