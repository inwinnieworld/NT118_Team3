# HƯỚNG DẪN XÂY DỰNG START SCREEN VÀ TÍCH HỢP AUTH FLOW

## 📋 MỤC LỤC
1. [Tổng quan kiến trúc hiện tại](#1-tổng-quan-kiến-trúc-hiện-tại)
2. [Phân tích Start Screen](#2-phân-tích-start-screen)
3. [Tư duy thiết kế Start Screen](#3-tư-duy-thiết-kế-start-screen)
4. [Hướng dẫn code Start Screen](#4-hướng-dẫn-code-start-screen)
5. [Tích hợp với Auth Flow](#5-tích-hợp-với-auth-flow)
6. [Roadmap triển khai](#6-roadmap-triển-khai)

---

## 1. TỔNG QUAN KIẾN TRÚC HIỆN TẠI

### 1.1. Cấu trúc Backend (NodeJS + MySQL)
```
backend/
├── src/
│   ├── config/
│   │   ├── db.js              # Kết nối MySQL
│   │   └── mailer.js          # Cấu hình gửi email
│   ├── controllers/
│   │   └── auth.controller.js # Xử lý request auth
│   ├── services/
│   │   └── auth.service.js    # Business logic auth
│   ├── routes/
│   │   └── auth.routes.js     # Định nghĩa API endpoints
│   └── server.js              # Entry point
```

**API Endpoints đã có:**
- `POST /api/auth/register` - Đăng ký tài khoản sinh viên
- `POST /api/auth/login` - Đăng nhập (email hoặc studentCode)
- `POST /api/auth/forgot-password` - Gửi email reset password
- `POST /api/auth/validate-reset-token` - Kiểm tra token hợp lệ
- `POST /api/auth/reset-password` - Đặt lại mật khẩu
- `GET /api/auth/open-reset-password` - Deep link handler

### 1.2. Cấu trúc Frontend (Android Java)
```
app/src/main/java/com/example/emotiondebugging/
├── ui/auth/
│   ├── LoginActivity.java           ✅ ĐÃ CODE
│   ├── LoginViewModel.java          ✅ ĐÃ CODE
│   ├── RegisterActivity.java        ✅ ĐÃ CODE
│   ├── RegisterViewModel.java       ✅ ĐÃ CODE
│   ├── ForgotPasswordActivity.java  ✅ ĐÃ CODE
│   ├── ForgotPasswordViewModel.java ✅ ĐÃ CODE
│   ├── ResetPasswordActivity.java   ✅ ĐÃ CODE
│   └── StartScreenActivity.java     ❌ CHƯA CODE (CẦN TẠO)
├── data/
│   ├── api/
│   │   ├── AuthApiService.java      ✅ ĐÃ CODE
│   │   └── RetrofitClient.java      ✅ ĐÃ CODE
│   └── repository/
│       └── AuthRepository.java      ✅ ĐÃ CODE
├── model/
│   ├── request/
│   │   ├── LoginRequest.java        ✅ ĐÃ CODE
│   │   ├── RegisterRequest.java     ✅ ĐÃ CODE
│   │   └── ...
│   └── response/
│       ├── LoginResponse.java       ✅ ĐÃ CODE
│       ├── ApiResponse.java         ✅ ĐÃ CODE
│       └── UserResponse.java        ✅ ĐÃ CODE
└── utils/
    ├── SharedPrefsHelper.java       ✅ ĐÃ CODE (giả định)
    └── Constants.java               ✅ ĐÃ CODE (giả định)
```

---

## 2. PHÂN TÍCH START SCREEN

### 2.1. Mục đích của Start Screen
Start Screen (hay Splash Screen) là màn hình đầu tiên xuất hiện khi mở app, có các chức năng:

1. **Branding**: Hiển thị logo, mascot của app
2. **Loading**: Khởi tạo các thành phần cần thiết (database, shared preferences, check network)
3. **Authentication Check**: Kiểm tra user đã đăng nhập chưa
4. **Navigation**: Điều hướng đến màn hình phù hợp:
   - Nếu đã login → MainActivity (Home Screen)
   - Nếu chưa login → LoginActivity

### 2.2. Yêu cầu từ Figma
Dựa vào mô tả, Start Screen cần có:
- **Background**: Gradient hoặc màu nền chủ đạo của app
- **Logo/Mascot**: Icon mascot của Emotion Debugging (có thể có hiệu ứng glow)
- **App Name**: "Emotion Debugging"
- **Loading Indicator**: ProgressBar hoặc animation
- **Thời gian hiển thị**: 2-3 giây

### 2.3. Flow Logic
```
[App Launch]
    ↓
[Start Screen hiển thị]
    ↓
[Khởi tạo: Database, SharedPrefs, Network Check]
    ↓
[Kiểm tra token trong SharedPreferences]
    ↓
    ├─→ [Có token hợp lệ] → MainActivity (Home)
    └─→ [Không có token] → LoginActivity
```

---

## 3. TƯ DUY THIẾT KẾ START SCREEN

### 3.1. Chuyển đổi Figma sang Android XML

#### Bước 1: Phân tích Figma Design
Khi nhìn vào Figma, bạn cần xác định:

**Layout Structure:**
- Root layout: `ConstraintLayout` hoặc `RelativeLayout`
- Background: Gradient drawable hoặc solid color
- Mascot: ImageView (center)
- App Name: TextView (below mascot)
- Loading: ProgressBar (bottom)

**Colors & Dimensions:**
- Background color: Lấy từ Figma (ví dụ: `#1A1A2E`)
- Mascot size: Width x Height (ví dụ: 120dp x 120dp)
- Text color: Lấy từ Figma (ví dụ: `#FFFFFF`)
- Text size: Lấy từ Figma (ví dụ: 24sp)

**Fonts:**
- Font family: Nếu Figma dùng custom font, cần import vào `res/font/`
- Font weight: Bold, Regular, etc.

#### Bước 2: Tạo Resources

**Colors (res/values/colors.xml):**
```xml
<color name="start_screen_bg">#1A1A2E</color>
<color name="start_screen_text">#FFFFFF</color>
<color name="primary_cyan">#20B8D9</color>
```

**Strings (res/values/strings.xml):**
```xml
<string name="app_name">Emotion Debugging</string>
<string name="app_tagline">Debug Your Emotions</string>
```

**Dimensions (res/values/dimens.xml):**
```xml
<dimen name="mascot_size">120dp</dimen>
<dimen name="app_name_text_size">24sp</dimen>
<dimen name="tagline_text_size">14sp</dimen>
```

**Drawable - Background Gradient (res/drawable/bg_start_screen.xml):**
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:angle="135"
        android:startColor="#1A1A2E"
        android:centerColor="#16213E"
        android:endColor="#0F3460"
        android:type="linear" />
</shape>
```

#### Bước 3: Tạo Layout XML

**activity_start_screen.xml:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_start_screen">

    <!-- Mascot Image -->
    <ImageView
        android:id="@+id/imgMascot"
        android:layout_width="@dimen/mascot_size"
        android:layout_height="@dimen/mascot_size"
        android:src="@drawable/ic_mascot"
        android:contentDescription="@string/app_name"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintVertical_bias="0.4" />

    <!-- App Name -->
    <TextView
        android:id="@+id/tvAppName"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/app_name"
        android:textSize="@dimen/app_name_text_size"
        android:textColor="@color/start_screen_text"
        android:textStyle="bold"
        android:layout_marginTop="24dp"
        app:layout_constraintTop_toBottomOf="@id/imgMascot"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Tagline -->
    <TextView
        android:id="@+id/tvTagline"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/app_tagline"
        android:textSize="@dimen/tagline_text_size"
        android:textColor="@color/primary_cyan"
        android:layout_marginTop="8dp"
        app:layout_constraintTop_toBottomOf="@id/tvAppName"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Loading Progress -->
    <ProgressBar
        android:id="@+id/progressBar"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:indeterminateTint="@color/primary_cyan"
        android:layout_marginBottom="48dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

### 3.2. Tối ưu hóa từ Figma

**Tips chuyển đổi chính xác:**

1. **Sử dụng Figma Inspect Mode:**
   - Click vào element → Tab "Inspect" → Copy CSS/Android values
   - Figma hiển thị: padding, margin, color hex, font size

2. **Export Assets:**
   - Select mascot/icons → Export → PNG (hdpi, xhdpi, xxhdpi, xxxhdpi)
   - Hoặc export SVG → convert sang Vector Drawable

3. **Match Colors Exactly:**
   - Dùng Color Picker trong Figma để lấy hex code chính xác
   - Lưu vào `colors.xml`

4. **Typography:**
   - Nếu Figma dùng custom font (ví dụ: Poppins, Roboto):
     - Download font → Copy vào `res/font/`
     - Sử dụng: `android:fontFamily="@font/poppins_bold"`

---

## 4. HƯỚNG DẪN CODE START SCREEN

### 4.1. Tạo StartScreenActivity.java

**File: `ui/auth/StartScreenActivity.java`**

```java
package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.ui.main.MainActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

/**
 * Start Screen (Splash Screen)
 * - Hiển thị logo/mascot khi mở app
 * - Khởi tạo các thành phần cần thiết
 * - Kiểm tra authentication status
 * - Điều hướng đến màn hình phù hợp
 */
public class StartScreenActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500; // 2.5 giây
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // Ẩn ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Khởi tạo SharedPrefsHelper
        prefsHelper = new SharedPrefsHelper(this);

        // Bắt đầu quá trình khởi tạo
        initializeApp();
    }

    /**
     * Khởi tạo các thành phần của app
     */
    private void initializeApp() {
        // Sử dụng Handler để delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthenticationStatus();
        }, SPLASH_DURATION);
    }

    /**
     * Kiểm tra trạng thái đăng nhập
     */
    private void checkAuthenticationStatus() {
        // Lấy token từ SharedPreferences
        String token = prefsHelper.getToken();

        if (token != null && !token.isEmpty()) {
            // Đã đăng nhập → Chuyển đến MainActivity
            navigateToMain();
        } else {
            // Chưa đăng nhập → Chuyển đến LoginActivity
            navigateToLogin();
        }
    }

    /**
     * Chuyển đến MainActivity (Home Screen)
     */
    private void navigateToMain() {
        Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Chuyển đến LoginActivity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(StartScreenActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
```

### 4.2. Cập nhật AndroidManifest.xml

**Thay đổi LAUNCHER activity từ LoginActivity sang StartScreenActivity:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.EmotionDebugging"
        android:usesCleartextTraffic="true">

        <!-- START SCREEN - LAUNCHER ACTIVITY -->
        <activity
            android:name=".ui.auth.StartScreenActivity"
            android:exported="true"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- LOGIN ACTIVITY -->
        <activity
            android:name=".ui.auth.LoginActivity"
            android:exported="false" />

        <!-- REGISTER ACTIVITY -->
        <activity
            android:name=".ui.auth.RegisterActivity"
            android:exported="false" />

        <!-- FORGOT PASSWORD ACTIVITY -->
        <activity
            android:name=".ui.auth.ForgotPasswordActivity"
            android:exported="false" />

        <!-- RESET PASSWORD ACTIVITY (Deep Link) -->
        <activity
            android:name=".ui.auth.ResetPasswordActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                <data
                    android:scheme="emotedebugging"
                    android:host="reset-password" />
            </intent-filter>
        </activity>

        <!-- MAIN ACTIVITY -->
        <activity
            android:name=".ui.main.MainActivity"
            android:exported="false" />

    </application>

</manifest>
```

### 4.3. Tạo Theme cho Start Screen

**File: `res/values/themes.xml`**

Thêm theme không có ActionBar:

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme -->
    <style name="Theme.EmotionDebugging" parent="Theme.MaterialComponents.DayNight.DarkActionBar">
        <item name="colorPrimary">@color/primary_cyan</item>
        <item name="colorPrimaryVariant">@color/primary_cyan</item>
        <item name="colorOnPrimary">@color/white</item>
    </style>

    <!-- Theme cho Start Screen (No ActionBar) -->
    <style name="Theme.EmotionDebugging.NoActionBar">
        <item name="windowActionBar">false</item>
        <item name="windowNoTitle">true</item>
        <item name="android:windowFullscreen">true</item>
    </style>
</resources>
```

### 4.4. Cập nhật SharedPrefsHelper

**File: `utils/SharedPrefsHelper.java`**

Đảm bảo có các methods cần thiết:

```java
package com.example.emotiondebugging.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsHelper {

    private static final String PREFS_NAME = "EmotionDebuggingPrefs";
    private static final String KEY_TOKEN = "auth_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences prefs;

    public SharedPrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Token methods
    public void saveToken(String token) {
        prefs.edit().putString(KEY_TOKEN, token).apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public void clearToken() {
        prefs.edit().remove(KEY_TOKEN).apply();
    }

    // User info methods
    public void saveUserInfo(int userId, String name, String email, String role) {
        prefs.edit()
                .putInt(KEY_USER_ID, userId)
                .putString(KEY_USER_NAME, name)
                .putString(KEY_USER_EMAIL, email)
                .putString(KEY_USER_ROLE, role)
                .apply();
    }

    public int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "");
    }

    // Check login status
    public boolean isLoggedIn() {
        return getToken() != null && !getToken().isEmpty();
    }

    // Clear all data (logout)
    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
```

---

## 5. TÍCH HỢP VỚI AUTH FLOW

### 5.1. Cập nhật LoginActivity

Sau khi login thành công, cần lưu token và user info:

```java
private void handleLoginSuccess(LoginResponse response) {
    // Lưu token và user info vào SharedPreferences
    SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
    prefsHelper.saveToken(response.getToken());
    
    if (response.getUser() != null) {
        prefsHelper.saveUserInfo(
            response.getUser().getUserId(),
            response.getUser().getName(),
            response.getUser().getEmail(),
            response.getUser().getRole()
        );
    }

    // Chuyển đến MainActivity
    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

### 5.2. Thêm Logout trong MainActivity

Khi user logout, cần clear token:

```java
public void logout() {
    SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
    prefsHelper.clearAll();

    // Chuyển về LoginActivity
    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

### 5.3. Flow hoàn chỉnh

```
[App Launch]
    ↓
[StartScreenActivity]
    ↓
    ├─→ [Có token] → MainActivity
    │                    ↓
    │                [User logout]
    │                    ↓
    └─→ [Không có token] → LoginActivity
                              ↓
                          [Login thành công]
                              ↓
                          [Lưu token]
                              ↓
                          MainActivity
```

---

## 6. ROADMAP TRIỂN KHAI

### Phase 1: Chuẩn bị Resources (30 phút)
1. ✅ Export assets từ Figma (mascot, icons)
2. ✅ Tạo `colors.xml` với màu từ Figma
3. ✅ Tạo `strings.xml` với text
4. ✅ Tạo `dimens.xml` với kích thước
5. ✅ Tạo `bg_start_screen.xml` (gradient background)

### Phase 2: Tạo Layout (30 phút)
1. ✅ Tạo `activity_start_screen.xml`
2. ✅ Sắp xếp các elements theo Figma
3. ✅ Test preview trong Android Studio

### Phase 3: Code Logic (45 phút)
1. ✅ Tạo `StartScreenActivity.java`
2. ✅ Implement initialization logic
3. ✅ Implement authentication check
4. ✅ Implement navigation logic

### Phase 4: Cập nhật Manifest (15 phút)
1. ✅ Thay đổi LAUNCHER activity
2. ✅ Thêm theme NoActionBar
3. ✅ Test app launch

### Phase 5: Tích hợp Auth (30 phút)
1. ✅ Cập nhật `SharedPrefsHelper`
2. ✅ Cập nhật `LoginActivity` để lưu token
3. ✅ Thêm logout trong `MainActivity`
4. ✅ Test full flow

### Phase 6: Testing (30 phút)
1. ✅ Test Start Screen hiển thị đúng
2. ✅ Test navigation khi chưa login
3. ✅ Test navigation khi đã login
4. ✅ Test logout và quay lại login

---

## 7. CHECKLIST HOÀN THÀNH

- [ ] Start Screen hiển thị đúng design Figma
- [ ] Mascot và text hiển thị rõ ràng
- [ ] Loading indicator hoạt động
- [ ] Kiểm tra token thành công
- [ ] Navigation đến LoginActivity khi chưa login
- [ ] Navigation đến MainActivity khi đã login
- [ ] Login thành công lưu token
- [ ] Logout xóa token và quay về Login
- [ ] Deep link reset password vẫn hoạt động
- [ ] App không crash khi launch

---

## 8. LƯU Ý QUAN TRỌNG

### 8.1. Performance
- Start Screen không nên quá 3 giây
- Nếu cần load data nặng, dùng AsyncTask hoặc Coroutines
- Tránh blocking UI thread

### 8.2. User Experience
- Không cho phép user back từ Start Screen
- Sử dụng `FLAG_ACTIVITY_CLEAR_TASK` để clear back stack
- Smooth transition giữa các màn hình

### 8.3. Security
- Token nên được mã hóa trong SharedPreferences (nâng cao)
- Kiểm tra token expiry (nếu backend hỗ trợ)
- Validate token với server (optional)

### 8.4. Testing
- Test trên nhiều kích thước màn hình
- Test trên Android versions khác nhau (minSdk 24)
- Test với network slow/offline

---

**File tiếp theo:** `PHAN_TICH_CHI_TIET_CODE_AUTH.md` sẽ giải thích chi tiết từng file code authentication hiện có.
