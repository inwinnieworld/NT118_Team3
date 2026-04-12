# HƯỚNG DẪN CODE START SCREEN TỪ FIGMA - ĐỘ CHÍNH XÁC 99%

## 📐 BƯỚC 1: PHÂN TÍCH FIGMA DESIGN

### Các thành phần trong Start Screen:

1. **Background Image**: Futuristic tech lab với tông màu cyan/dark teal
2. **Logo Text "emotion"**: Chữ thường, màu cyan glow, font size lớn
3. **Logo Text "Debugging_"**: Có underscore nhấp nháy, màu cyan
4. **Mascot/Bug Character**: Robot bug ở giữa màn hình
5. **Progress Bar**: Thanh loading gradient cyan ở dưới cùng

### Kích thước và vị trí (ước lượng từ Figma):
- Screen: 375 x 812 (iPhone X size)
- Logo "emotion": ~200dp width, positioned at 40% from top
- Logo "Debugging_": Below "emotion", ~250dp width
- Mascot: ~120dp x 120dp, centered
- Progress Bar: Full width - 48dp margin, height ~8dp, positioned 80dp from bottom

---

## 🎨 BƯỚC 2: CHUẨN BỊ RESOURCES

### 2.1. Colors (res/values/colors.xml)

Thêm các màu từ Figma:

```xml
<!-- Start Screen Colors -->
<color name="start_screen_bg_dark">#0A1929</color>
<color name="start_screen_bg_teal">#0D2A3F</color>
<color name="cyan_primary">#00D9FF</color>
<color name="cyan_glow">#00FFFF</color>
<color name="cyan_dark">#008B9E</color>
<color name="progress_bg">#1A3A4F</color>
<color name="progress_fill">#00D9FF</color>
```

### 2.2. Dimensions (res/values/dimens.xml)

```xml
<!-- Start Screen Dimensions -->
<dimen name="start_logo_emotion_size">48sp</dimen>
<dimen name="start_logo_debugging_size">32sp</dimen>
<dimen name="start_mascot_size">120dp</dimen>
<dimen name="start_progress_height">8dp</dimen>
<dimen name="start_progress_margin">48dp</dimen>
<dimen name="start_progress_bottom_margin">80dp</dimen>
```

### 2.3. Strings (res/values/strings.xml)

```xml
<!-- Start Screen Strings -->
<string name="start_logo_emotion">emotion</string>
<string name="start_logo_debugging">Debugging_</string>
<string name="start_loading">Loading...</string>
```

### 2.4. Background Drawable (res/drawable/bg_start_screen.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Base dark gradient -->
    <item>
        <shape android:shape="rectangle">
            <gradient
                android:angle="135"
                android:startColor="#0A1929"
                android:centerColor="#0D2A3F"
                android:endColor="#0F3A52"
                android:type="linear" />
        </shape>
    </item>
    
    <!-- Optional: Overlay image nếu có export từ Figma -->
    <!-- <item>
        <bitmap
            android:src="@drawable/start_screen_bg"
            android:gravity="fill"
            android:alpha="0.8" />
    </item> -->
</layer-list>
```

### 2.5. Progress Bar Background (res/drawable/bg_progress_bar.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="100dp" />
    <solid android:color="@color/progress_bg" />
</shape>
```

### 2.6. Progress Bar Fill (res/drawable/bg_progress_fill.xml)

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <corners android:radius="100dp" />
    <gradient
        android:angle="0"
        android:startColor="#00D9FF"
        android:centerColor="#00FFFF"
        android:endColor="#00D9FF"
        android:type="linear" />
</shape>
```

### 2.7. Text Glow Effect (res/values/styles.xml)

```xml
<style name="StartScreenLogoStyle">
    <item name="android:textColor">@color/cyan_primary</item>
    <item name="android:shadowColor">@color/cyan_glow</item>
    <item name="android:shadowDx">0</item>
    <item name="android:shadowDy">0</item>
    <item name="android:shadowRadius">20</item>
    <item name="android:fontFamily">sans-serif-medium</item>
</style>
```

---

## 📱 BƯỚC 3: TẠO LAYOUT XML

### File: res/layout/activity_start_screen.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_start_screen"
    tools:context=".ui.auth.StartScreenActivity">

    <!-- Background Image (nếu có export từ Figma) -->
    <ImageView
        android:id="@+id/imgBackground"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:alpha="0.6"
        android:contentDescription="Background"
        android:src="@drawable/start_screen_bg"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- Logo Container -->
    <LinearLayout
        android:id="@+id/logoContainer"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/mascotContainer"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintVertical_bias="0.35">

        <!-- "emotion" Text -->
        <TextView
            android:id="@+id/tvLogoEmotion"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/start_logo_emotion"
            android:textSize="@dimen/start_logo_emotion_size"
            android:textStyle="bold"
            style="@style/StartScreenLogoStyle"
            android:letterSpacing="0.05" />

        <!-- "Debugging_" Text -->
        <TextView
            android:id="@+id/tvLogoDebugging"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/start_logo_debugging"
            android:textSize="@dimen/start_logo_debugging_size"
            android:textStyle="bold"
            style="@style/StartScreenLogoStyle"
            android:layout_marginTop="4dp"
            android:letterSpacing="0.08" />
    </LinearLayout>

    <!-- Mascot/Bug Character -->
    <FrameLayout
        android:id="@+id/mascotContainer"
        android:layout_width="@dimen/start_mascot_size"
        android:layout_height="@dimen/start_mascot_size"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintVertical_bias="0.5">

        <!-- Glow effect background -->
        <View
            android:id="@+id/viewMascotGlow"
            android:layout_width="140dp"
            android:layout_height="140dp"
            android:layout_gravity="center"
            android:background="@drawable/bg_mascot_glow_cyan"
            android:alpha="0.6" />

        <!-- Mascot Image -->
        <ImageView
            android:id="@+id/imgMascot"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:src="@drawable/ic_mascot"
            android:contentDescription="Mascot"
            android:scaleType="fitCenter" />
    </FrameLayout>

    <!-- Progress Bar Container -->
    <LinearLayout
        android:id="@+id/progressContainer"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:layout_marginStart="@dimen/start_progress_margin"
        android:layout_marginEnd="@dimen/start_progress_margin"
        android:layout_marginBottom="@dimen/start_progress_bottom_margin"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">

        <!-- Progress Bar -->
        <FrameLayout
            android:layout_width="match_parent"
            android:layout_height="@dimen/start_progress_height">

            <!-- Background -->
            <View
                android:id="@+id/progressBackground"
                android:layout_width="match_parent"
                android:layout_height="match_parent"
                android:background="@drawable/bg_progress_bar" />

            <!-- Fill (animated) -->
            <View
                android:id="@+id/progressFill"
                android:layout_width="0dp"
                android:layout_height="match_parent"
                android:background="@drawable/bg_progress_fill" />
        </FrameLayout>

        <!-- Loading Text (optional) -->
        <TextView
            android:id="@+id/tvLoading"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/start_loading"
            android:textColor="@color/cyan_primary"
            android:textSize="12sp"
            android:layout_marginTop="12dp"
            android:alpha="0.7"
            android:visibility="gone" />
    </LinearLayout>

</androidx.constraintlayout.widget.ConstraintLayout>
```

---

## 💻 BƯỚC 4: TẠO STARTSCREENACTIVITY.JAVA

### File: ui/auth/StartScreenActivity.java

```java
package com.example.emotiondebugging.ui.auth;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.ui.main.MainActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

/**
 * Start Screen Activity
 * - Hiển thị logo và mascot
 * - Animate progress bar từ 0% đến 100%
 * - Check authentication status
 * - Navigate đến LoginActivity hoặc MainActivity
 */
public class StartScreenActivity extends AppCompatActivity {

    // UI Components
    private View progressFill;
    private TextView tvLogoDebugging;
    
    // Animation
    private ValueAnimator progressAnimator;
    private static final int ANIMATION_DURATION = 2500; // 2.5 giây
    
    // Helper
    private SharedPrefsHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);

        // Ẩn ActionBar và Status Bar để fullscreen
        hideSystemUI();

        // Khởi tạo views
        initViews();

        // Khởi tạo SharedPrefsHelper
        prefsHelper = new SharedPrefsHelper(this);

        // Bắt đầu animation
        startLoadingAnimation();
    }

    /**
     * Ẩn system UI để fullscreen
     */
    private void hideSystemUI() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        
        // Fullscreen mode
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    /**
     * Khởi tạo views
     */
    private void initViews() {
        progressFill = findViewById(R.id.progressFill);
        tvLogoDebugging = findViewById(R.id.tvLogoDebugging);
        
        // Set initial progress width to 0
        progressFill.getLayoutParams().width = 0;
        progressFill.requestLayout();
    }

    /**
     * Bắt đầu animation loading
     */
    private void startLoadingAnimation() {
        // Animate underscore blinking
        animateUnderscoreBlink();
        
        // Animate progress bar
        animateProgressBar();
    }

    /**
     * Animate underscore nhấp nháy trong "Debugging_"
     */
    private void animateUnderscoreBlink() {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable blinkRunnable = new Runnable() {
            boolean showUnderscore = true;
            
            @Override
            public void run() {
                if (showUnderscore) {
                    tvLogoDebugging.setText("Debugging_");
                } else {
                    tvLogoDebugging.setText("Debugging");
                }
                showUnderscore = !showUnderscore;
                handler.postDelayed(this, 500); // Blink every 500ms
            }
        };
        handler.post(blinkRunnable);
    }

    /**
     * Animate progress bar từ 0% đến 100%
     */
    private void animateProgressBar() {
        // Get parent width để tính toán
        progressFill.post(() -> {
            int parentWidth = ((View) progressFill.getParent()).getWidth();
            
            // Create animator
            progressAnimator = ValueAnimator.ofInt(0, parentWidth);
            progressAnimator.setDuration(ANIMATION_DURATION);
            progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
            
            progressAnimator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                progressFill.getLayoutParams().width = value;
                progressFill.requestLayout();
            });
            
            // Khi animation kết thúc, check authentication
            progressAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    // Delay thêm 300ms để user thấy progress bar đầy
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        checkAuthenticationAndNavigate();
                    }, 300);
                }
            });
            
            // Start animation
            progressAnimator.start();
        });
    }

    /**
     * Kiểm tra authentication status và navigate
     */
    private void checkAuthenticationAndNavigate() {
        // Lấy token từ SharedPreferences
        String token = prefsHelper.getToken();

        if (token != null && !token.isEmpty()) {
            // Đã đăng nhập → Navigate to MainActivity
            navigateToMain();
        } else {
            // Chưa đăng nhập → Navigate to LoginActivity
            navigateToLogin();
        }
    }

    /**
     * Navigate đến MainActivity
     */
    private void navigateToMain() {
        Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    /**
     * Navigate đến LoginActivity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(StartScreenActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
        // Smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel animation nếu activity bị destroy
        if (progressAnimator != null && progressAnimator.isRunning()) {
            progressAnimator.cancel();
        }
    }

    @Override
    public void onBackPressed() {
        // Disable back button trong Start Screen
        // User không thể back từ màn hình này
    }
}
```

---

## 🎨 BƯỚC 5: EXPORT ASSETS TỪ FIGMA

### 5.1. Export Background Image

1. Trong Figma, select background layer
2. Export Settings:
   - Format: PNG
   - Scale: @1x, @2x, @3x, @4x (cho hdpi, xhdpi, xxhdpi, xxxhdpi)
3. Đặt tên: `start_screen_bg.png`
4. Copy vào:
   - `res/drawable-hdpi/start_screen_bg.png`
   - `res/drawable-xhdpi/start_screen_bg.png`
   - `res/drawable-xxhdpi/start_screen_bg.png`
   - `res/drawable-xxxhdpi/start_screen_bg.png`

### 5.2. Export Mascot/Bug Character

1. Select mascot layer trong Figma
2. Export Settings:
   - Format: PNG (hoặc SVG nếu muốn vector)
   - Scale: @1x, @2x, @3x, @4x
3. Đặt tên: `ic_mascot.png`
4. Copy vào các thư mục drawable tương ứng

**Hoặc export SVG:**
1. Export as SVG
2. Convert sang Vector Drawable bằng Android Studio:
   - Right click `res/drawable` → New → Vector Asset
   - Local file → chọn SVG file
   - Đặt tên: `ic_mascot.xml`

---

## 📝 BƯỚC 6: CẬP NHẬT ANDROIDMANIFEST.XML

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
            android:screenOrientation="portrait"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- LOGIN ACTIVITY -->
        <activity
            android:name=".ui.auth.LoginActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <!-- REGISTER ACTIVITY -->
        <activity
            android:name=".ui.auth.RegisterActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <!-- FORGOT PASSWORD ACTIVITY -->
        <activity
            android:name=".ui.auth.ForgotPasswordActivity"
            android:exported="false"
            android:screenOrientation="portrait" />

        <!-- RESET PASSWORD ACTIVITY -->
        <activity
            android:name=".ui.auth.ResetPasswordActivity"
            android:exported="true"
            android:screenOrientation="portrait">
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
            android:exported="false"
            android:screenOrientation="portrait" />

    </application>

</manifest>
```

---

## 🎯 BƯỚC 7: IMPLEMENT SHAREDPREFSHELPER

### File: utils/SharedPrefsHelper.java

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

## ✅ CHECKLIST HOÀN THÀNH

- [ ] Export background image từ Figma
- [ ] Export mascot image từ Figma
- [ ] Tạo colors.xml với màu từ Figma
- [ ] Tạo dimens.xml
- [ ] Tạo strings.xml
- [ ] Tạo bg_start_screen.xml
- [ ] Tạo bg_progress_bar.xml
- [ ] Tạo bg_progress_fill.xml
- [ ] Tạo style StartScreenLogoStyle
- [ ] Tạo activity_start_screen.xml
- [ ] Tạo StartScreenActivity.java
- [ ] Implement SharedPrefsHelper.java
- [ ] Cập nhật AndroidManifest.xml
- [ ] Test animation progress bar
- [ ] Test navigation to Login/Main

---

## 🎨 TIPS ĐỂ ĐẠT 99% CHÍNH XÁC

1. **Sử dụng Figma Inspect Mode:**
   - Click vào từng element
   - Copy exact color hex codes
   - Copy exact font sizes
   - Copy exact spacing/margins

2. **Export Assets đúng resolution:**
   - @1x = mdpi (baseline)
   - @1.5x = hdpi
   - @2x = xhdpi
   - @3x = xxhdpi
   - @4x = xxxhdpi

3. **Match fonts:**
   - Nếu Figma dùng custom font, download và add vào `res/font/`
   - Sử dụng: `android:fontFamily="@font/your_font"`

4. **Test trên nhiều màn hình:**
   - Small (480x800)
   - Normal (720x1280)
   - Large (1080x1920)
   - XLarge (1440x2560)

5. **Animation timing:**
   - Adjust ANIMATION_DURATION để match với cảm giác của Figma prototype
   - Test trên thiết bị thật, không chỉ emulator

---

**File tiếp theo sẽ là code implementation thực tế!**
