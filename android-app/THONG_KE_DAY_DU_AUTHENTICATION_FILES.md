# 📋 THỐNG KÊ ĐẦY ĐỦ TẤT CẢ FILES AUTHENTICATION

> **Tài liệu này liệt kê ĐẦY ĐỦ tất cả các files tham gia vào 4 chức năng:**
> - ✅ Start Screen (Màn hình khởi động)
> - ✅ Login (Đăng nhập)
> - ✅ Register (Đăng ký)
> - ✅ Forgot Password (Quên mật khẩu)
> - ✅ Reset Password (Đặt lại mật khẩu)

---

## 📊 TỔNG QUAN CẤU TRÚC

### Tổng số files: **30 files**

| Category | Số lượng | Mô tả |
|----------|----------|-------|
| **Activities** | 5 files | Các màn hình UI chính |
| **ViewModels** | 3 files | Xử lý logic nghiệp vụ (MVVM) |
| **Repository** | 1 file | Trung gian giữa ViewModel và API |
| **API Service** | 2 files | Retrofit API interface và Client |
| **Models - Request** | 5 files | Dữ liệu gửi lên server |
| **Models - Response** | 3 files | Dữ liệu nhận từ server |
| **Layouts XML** | 5 files | Giao diện màn hình |
| **Drawables XML** | 8 files | Background, button, input styles |
| **Utils** | 1 file | SharedPrefsHelper (lưu token) |
| **AndroidManifest** | 1 file | Cấu hình app |

---

## 🎯 PHẦN 1: ACTIVITIES (5 FILES)

### 1.1. StartScreenActivity.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/ui/splash/StartScreenActivity.java`

**Chức năng:** Màn hình khởi động đầu tiên khi mở app, hiển thị logo và progress bar loading

```java
package com.example.emotiondebugging.ui.splash;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ProgressBar;
import androidx.appcompat.app.AppCompatActivity;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.ui.auth.LoginActivity;

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Làm màn hình tràn viền hoàn toàn (fullscreen)
        // Ẩn status bar và navigation bar để hiển thị toàn màn hình
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        // ✅ Set layout cho activity này
        setContentView(R.layout.activity_start_screen);

        // ✅ Lấy reference đến ProgressBar từ layout
        ProgressBar progressBar = findViewById(R.id.progressBar);

        // ✅ Tạo animation cho progress bar
        // Chạy loading từ 0 đến 100 trong 5.5 giây
        ObjectAnimator animation = ObjectAnimator.ofInt(progressBar, "progress", 0, 100);
        animation.setDuration(5500); // Thời gian animation: 5.5 giây
        
        // ✅ DecelerateInterpolator: Chạy nhanh lúc đầu, chậm dần về cuối (smooth effect)
        animation.setInterpolator(new android.view.animation.DecelerateInterpolator());
        
        // ✅ Listener để xử lý khi animation kết thúc
        animation.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // ✅ Khi loading xong, chuyển sang màn hình Login
                Intent intent = new Intent(StartScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                
                // ✅ Thêm hiệu ứng fade in/out khi chuyển màn hình
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                
                // ✅ Đóng StartScreen để user không quay lại được
                finish();
            }
        });

        // ✅ Bắt đầu chạy animation
        animation.start();
    }
}
```

**📌 Lưu ý quan trọng:**
- ⚠️ **VẤN ĐỀ:** Activity này KHÔNG kiểm tra authentication state
- ⚠️ **HẬU QUẢ:** User phải login lại mỗi lần mở app
- ✅ **GIẢI PHÁP:** Cần thêm logic check token trong SharedPrefs trước khi quyết định chuyển đến Login hay MainActivity

---

### 1.2. LoginActivity.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/ui/auth/LoginActivity.java`

**Chức năng:** Màn hình đăng nhập, cho phép user nhập account/password và đăng nhập

```java
package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.LoginResponse;

public class LoginActivity extends AppCompatActivity {

    // ✅ Khai báo các view components
    private EditText etAccount;        // Input nhập tài khoản (email hoặc student code)
    private EditText etPassword;       // Input nhập mật khẩu
    private ImageView imgTogglePassword; // Icon con mắt để show/hide password
    private Button btnLogin;           // Nút đăng nhập
    private TextView tvForgotPassword; // Text link "Quên mật khẩu?"
    private TextView tvRegister;       // Text link "Đăng ký"

    private boolean isPasswordVisible = false; // Trạng thái hiển thị password
    private LoginViewModel viewModel;          // ViewModel xử lý logic đăng nhập

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ✅ Ẩn ActionBar (thanh tiêu đề mặc định của Android)
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ✅ Khởi tạo các components
        initViews();      // Liên kết views với code
        initViewModel();  // Setup ViewModel và observers
        initActions();    // Gắn sự kiện click cho các buttons
    }

    /**
     * ✅ Liên kết các view components từ XML layout với code Java
     */
    private void initViews() {
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    /**
     * ✅ Khởi tạo ViewModel và observe (theo dõi) các LiveData
     * Khi dữ liệu trong ViewModel thay đổi, UI sẽ tự động cập nhật
     */
    private void initViewModel() {
        // ✅ Tạo instance của LoginViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // ✅ Observe message: Hiển thị thông báo lỗi hoặc thành công
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Observe loading state: Disable button và đổi text khi đang loading
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            btnLogin.setEnabled(!isLoading); // Disable button khi đang loading
            btnLogin.setText(isLoading ? "Đang đăng nhập..." : "Đăng nhập");
        });

        // ✅ Observe login response: Xử lý khi đăng nhập thành công
        viewModel.getLoginResponse().observe(this, response -> {
            if (response != null && response.getUser() != null) {
                handleLoginSuccess(response);
            }
        });
    }

    /**
     * ✅ Gắn các sự kiện click cho buttons và text links
     */
    private void initActions() {
        // ✅ Click icon con mắt để show/hide password
        imgTogglePassword.setOnClickListener(v -> togglePassword());

        // ✅ Click nút đăng nhập
        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            viewModel.login(account, password); // Gọi ViewModel để xử lý login
        });

        // ✅ Click "Quên mật khẩu?" -> Chuyển sang ForgotPasswordActivity
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // ✅ Click "Đăng ký" -> Chuyển sang RegisterActivity
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * ✅ Xử lý khi đăng nhập thành công
     * @param response Dữ liệu trả về từ server (token + user info)
     */
    private void handleLoginSuccess(LoginResponse response) {
        String role = response.getUser().getRole() != null ? response.getUser().getRole() : "";
        Toast.makeText(this, "Đăng nhập thành công - " + role, Toast.LENGTH_SHORT).show();
        
        // ⚠️ VẤN ĐỀ NGHIÊM TRỌNG: Không lưu token vào SharedPrefs!
        // ⚠️ HẬU QUẢ: User phải login lại mỗi lần mở app
        // ✅ CẦN THÊM: SharedPrefsHelper.saveToken(response.getToken());
        // ✅ CẦN THÊM: Chuyển sang MainActivity sau khi lưu token
    }

    /**
     * ✅ Toggle hiển thị/ẩn password
     */
    private void togglePassword() {
        if (isPasswordVisible) {
            // Ẩn password (hiển thị dấu chấm)
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            // Hiển thị password (text rõ ràng)
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }

        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length()); // Giữ cursor ở cuối
    }
}
```

**📌 Lưu ý quan trọng:**
- ⚠️ **VẤN ĐỀ NGHIÊM TRỌNG:** Không lưu token sau khi login thành công
- ⚠️ **VẤN ĐỀ:** Không chuyển sang MainActivity sau khi login
- ✅ **GIẢI PHÁP:** Cần implement SharedPrefsHelper và lưu token, sau đó chuyển màn hình

---
### 1.3. RegisterActivity.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/ui/auth/RegisterActivity.java`

**Chức năng:** Màn hình đăng ký tài khoản mới cho user

```java
package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;

public class RegisterActivity extends AppCompatActivity {

    // ✅ Khai báo các view components
    private EditText etStudentCode;    // Input mã sinh viên
    private EditText etEmail;          // Input email
    private EditText etPassword;       // Input mật khẩu
    private EditText etConfirmPassword; // Input xác nhận mật khẩu
    private ImageView imgTogglePassword;        // Icon show/hide password
    private ImageView imgToggleConfirmPassword; // Icon show/hide confirm password
    private Button btnRegister;        // Nút đăng ký
    private TextView tvLogin;          // Text link "Đã có tài khoản? Đăng nhập"

    private boolean isPasswordVisible = false;        // Trạng thái hiển thị password
    private boolean isConfirmPasswordVisible = false; // Trạng thái hiển thị confirm password
    private RegisterViewModel viewModel;              // ViewModel xử lý logic đăng ký

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ✅ Ẩn ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ✅ Khởi tạo các components
        initViews();
        initViewModel();
        initActions();
    }

    /**
     * ✅ Liên kết các view components từ XML layout
     */
    private void initViews() {
        etStudentCode = findViewById(R.id.etStudentCode);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    /**
     * ✅ Khởi tạo ViewModel và observe LiveData
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // ✅ Observe message: Hiển thị thông báo
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Observe loading state
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            btnRegister.setEnabled(!isLoading);
            btnRegister.setText(isLoading ? "Đang đăng ký..." : "Đăng ký");
        });

        // ✅ Observe register success: Chuyển về màn hình Login
        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Đăng ký thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                
                // ✅ Chuyển về LoginActivity
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Clear stack
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * ✅ Gắn các sự kiện click
     */
    private void initActions() {
        // ✅ Toggle password visibility
        imgTogglePassword.setOnClickListener(v -> togglePassword());
        imgToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPassword());

        // ✅ Click nút đăng ký
        btnRegister.setOnClickListener(v -> {
            String studentCode = etStudentCode.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // ✅ Gọi ViewModel để xử lý đăng ký
            viewModel.register(studentCode, email, password, confirmPassword);
        });

        // ✅ Click "Đã có tài khoản? Đăng nhập" -> Quay về LoginActivity
        tvLogin.setOnClickListener(v -> {
            finish(); // Đóng RegisterActivity, quay về LoginActivity
        });
    }

    /**
     * ✅ Toggle hiển thị/ẩn password
     */
    private void togglePassword() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    /**
     * ✅ Toggle hiển thị/ẩn confirm password
     */
    private void toggleConfirmPassword() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Có validation password matching trong ViewModel
- ✅ **ĐIỂM MẠNH:** Chuyển về LoginActivity sau khi đăng ký thành công
- ⚠️ **VẤN ĐỀ:** Không có validation format email và độ mạnh password ở UI layer

---

### 1.4. ForgotPasswordActivity.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/ui/auth/ForgotPasswordActivity.java`

**Chức năng:** Màn hình quên mật khẩu, gửi OTP về email để reset password

```java
package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    // ✅ Khai báo các view components
    private EditText etEmail;          // Input nhập email
    private Button btnSendOTP;         // Nút gửi OTP
    private ImageView imgBack;         // Icon back về màn hình trước
    private ForgotPasswordViewModel viewModel; // ViewModel xử lý logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // ✅ Ẩn ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ✅ Khởi tạo các components
        initViews();
        initViewModel();
        initActions();
    }

    /**
     * ✅ Liên kết các view components từ XML layout
     */
    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        imgBack = findViewById(R.id.imgBack);
    }

    /**
     * ✅ Khởi tạo ViewModel và observe LiveData
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        // ✅ Observe message: Hiển thị thông báo
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Observe loading state
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            btnSendOTP.setEnabled(!isLoading);
            btnSendOTP.setText(isLoading ? "Đang gửi..." : "Gửi mã OTP");
        });

        // ✅ Observe OTP sent success: Chuyển sang ResetPasswordActivity
        viewModel.getOtpSent().observe(this, success -> {
            if (success != null && success) {
                String email = etEmail.getText().toString().trim();
                
                // ✅ Chuyển sang ResetPasswordActivity và truyền email qua Intent
                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * ✅ Gắn các sự kiện click
     */
    private void initActions() {
        // ✅ Click nút back -> Quay về màn hình trước
        imgBack.setOnClickListener(v -> finish());

        // ✅ Click nút gửi OTP
        btnSendOTP.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            viewModel.sendOTP(email); // Gọi ViewModel để gửi OTP
        });
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Truyền email sang ResetPasswordActivity qua Intent
- ✅ **ĐIỂM MẠNH:** Có loading state và disable button khi đang gửi
- ⚠️ **VẤN ĐỀ:** Không có validation format email trước khi gửi

---

### 1.5. ResetPasswordActivity.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/ui/auth/ResetPasswordActivity.java`

**Chức năng:** Màn hình đặt lại mật khẩu, nhập OTP và mật khẩu mới

```java
package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;

public class ResetPasswordActivity extends AppCompatActivity {

    // ✅ Khai báo các view components
    private EditText etOTP;            // Input nhập mã OTP
    private EditText etNewPassword;    // Input nhập mật khẩu mới
    private EditText etConfirmPassword; // Input xác nhận mật khẩu mới
    private ImageView imgTogglePassword;        // Icon show/hide password
    private ImageView imgToggleConfirmPassword; // Icon show/hide confirm password
    private Button btnResetPassword;   // Nút đặt lại mật khẩu
    private ImageView imgBack;         // Icon back

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private ForgotPasswordViewModel viewModel; // Dùng chung ViewModel với ForgotPassword

    private String email; // Email nhận từ ForgotPasswordActivity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // ✅ Ẩn ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // ✅ Lấy email từ Intent
        email = getIntent().getStringExtra("email");
        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không tìm thấy email", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // ✅ Khởi tạo các components
        initViews();
        initViewModel();
        initActions();
    }

    /**
     * ✅ Liên kết các view components từ XML layout
     */
    private void initViews() {
        etOTP = findViewById(R.id.etOTP);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        imgToggleConfirmPassword = findViewById(R.id.imgToggleConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        imgBack = findViewById(R.id.imgBack);
    }

    /**
     * ✅ Khởi tạo ViewModel và observe LiveData
     */
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        // ✅ Observe message
        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Observe loading state
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            btnResetPassword.setEnabled(!isLoading);
            btnResetPassword.setText(isLoading ? "Đang xử lý..." : "Đặt lại mật khẩu");
        });

        // ✅ Observe reset success: Chuyển về LoginActivity
        viewModel.getResetSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Đặt lại mật khẩu thành công! Vui lòng đăng nhập.", Toast.LENGTH_LONG).show();
                
                // ✅ Chuyển về LoginActivity
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * ✅ Gắn các sự kiện click
     */
    private void initActions() {
        // ✅ Click back
        imgBack.setOnClickListener(v -> finish());

        // ✅ Toggle password visibility
        imgTogglePassword.setOnClickListener(v -> togglePassword());
        imgToggleConfirmPassword.setOnClickListener(v -> toggleConfirmPassword());

        // ✅ Click nút đặt lại mật khẩu
        btnResetPassword.setOnClickListener(v -> {
            String otp = etOTP.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            // ✅ Gọi ViewModel để reset password
            viewModel.resetPassword(email, otp, newPassword, confirmPassword);
        });
    }

    /**
     * ✅ Toggle hiển thị/ẩn password
     */
    private void togglePassword() {
        if (isPasswordVisible) {
            etNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            etNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isPasswordVisible = !isPasswordVisible;
        etNewPassword.setSelection(etNewPassword.getText().length());
    }

    /**
     * ✅ Toggle hiển thị/ẩn confirm password
     */
    private void toggleConfirmPassword() {
        if (isConfirmPasswordVisible) {
            etConfirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            etConfirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
        isConfirmPasswordVisible = !isConfirmPasswordVisible;
        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Nhận email từ Intent và truyền vào API
- ✅ **ĐIỂM MẠNH:** Clear activity stack khi chuyển về Login
- ⚠️ **VẤN ĐỀ:** Không có validation OTP format (6 digits)

---

## 🧠 PHẦN 2: VIEWMODELS (3 FILES)

### 2.1. LoginViewModel.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/viewmodel/LoginViewModel.java`

**Chức năng:** Xử lý logic nghiệp vụ cho màn hình Login (MVVM pattern)

```java
package com.example.emotiondebugging.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.response.LoginResponse;
import com.example.emotiondebugging.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginViewModel extends ViewModel {

    // ✅ Repository để gọi API
    private final AuthRepository repository = new AuthRepository();

    // ✅ LiveData để UI observe (theo dõi)
    private final MutableLiveData<LoginResponse> loginResponse = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // ✅ Getter methods để Activity observe
    public LiveData<LoginResponse> getLoginResponse() {
        return loginResponse;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * ✅ Xử lý logic đăng nhập
     * @param account Tài khoản (email hoặc student code)
     * @param password Mật khẩu
     */
    public void login(String account, String password) {
        // ✅ Validation input
        if (account.isEmpty()) {
            message.setValue("Vui lòng nhập tài khoản");
            return;
        }

        if (password.isEmpty()) {
            message.setValue("Vui lòng nhập mật khẩu");
            return;
        }

        // ✅ Set loading state
        loading.setValue(true);

        // ✅ Tạo request object
        LoginRequest request = new LoginRequest(account, password);

        // ✅ Gọi API qua Repository
        repository.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                loading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Đăng nhập thành công
                    loginResponse.setValue(response.body());
                    message.setValue("Đăng nhập thành công");
                } else {
                    // ❌ Lỗi từ server (401, 400, etc.)
                    message.setValue("Đăng nhập thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loading.setValue(false);
                // ❌ Lỗi network hoặc exception
                message.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Có validation input trước khi gọi API
- ✅ **ĐIỂM MẠNH:** Xử lý cả success và error cases
- ⚠️ **VẤN ĐỀ:** Không parse error message từ response body

---

### 2.2. RegisterViewModel.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/viewmodel/RegisterViewModel.java`

**Chức năng:** Xử lý logic nghiệp vụ cho màn hình Register

```java
package com.example.emotiondebugging.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.model.request.RegisterRequest;
import com.example.emotiondebugging.model.response.RegisterResponse;
import com.example.emotiondebugging.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterViewModel extends ViewModel {

    // ✅ Repository để gọi API
    private final AuthRepository repository = new AuthRepository();

    // ✅ LiveData để UI observe
    private final MutableLiveData<RegisterResponse> registerResponse = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // ✅ Getter methods
    public LiveData<RegisterResponse> getRegisterResponse() {
        return registerResponse;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * ✅ Xử lý logic đăng ký
     * @param studentCode Mã sinh viên
     * @param email Email
     * @param password Mật khẩu
     * @param confirmPassword Xác nhận mật khẩu
     */
    public void register(String studentCode, String email, String password, String confirmPassword) {
        // ✅ Validation input
        if (studentCode.isEmpty()) {
            message.setValue("Vui lòng nhập mã sinh viên");
            return;
        }

        if (email.isEmpty()) {
            message.setValue("Vui lòng nhập email");
            return;
        }

        if (password.isEmpty()) {
            message.setValue("Vui lòng nhập mật khẩu");
            return;
        }

        if (confirmPassword.isEmpty()) {
            message.setValue("Vui lòng xác nhận mật khẩu");
            return;
        }

        // ✅ Kiểm tra password matching
        if (!password.equals(confirmPassword)) {
            message.setValue("Mật khẩu xác nhận không khớp");
            return;
        }

        // ✅ Set loading state
        loading.setValue(true);

        // ✅ Tạo request object
        RegisterRequest request = new RegisterRequest(studentCode, email, password);

        // ✅ Gọi API qua Repository
        repository.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                loading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Đăng ký thành công
                    registerResponse.setValue(response.body());
                    registerSuccess.setValue(true);
                    message.setValue("Đăng ký thành công");
                } else {
                    // ❌ Lỗi từ server
                    message.setValue("Đăng ký thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                loading.setValue(false);
                // ❌ Lỗi network
                message.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Có validation password matching
- ✅ **ĐIỂM MẠNH:** Có LiveData registerSuccess để trigger navigation
- ⚠️ **VẤN ĐỀ:** Không validate format email và độ mạnh password

---

### 2.3. ForgotPasswordViewModel.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/viewmodel/ForgotPasswordViewModel.java`

**Chức năng:** Xử lý logic cho cả Forgot Password và Reset Password

```java
package com.example.emotiondebugging.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.emotiondebugging.model.request.ForgotPasswordRequest;
import com.example.emotiondebugging.model.request.ResetPasswordRequest;
import com.example.emotiondebugging.model.response.MessageResponse;
import com.example.emotiondebugging.repository.AuthRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordViewModel extends ViewModel {

    // ✅ Repository để gọi API
    private final AuthRepository repository = new AuthRepository();

    // ✅ LiveData để UI observe
    private final MutableLiveData<Boolean> otpSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> resetSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    // ✅ Getter methods
    public LiveData<Boolean> getOtpSent() {
        return otpSent;
    }

    public LiveData<Boolean> getResetSuccess() {
        return resetSuccess;
    }

    public LiveData<String> getMessage() {
        return message;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    /**
     * ✅ Gửi OTP về email
     * @param email Email người dùng
     */
    public void sendOTP(String email) {
        // ✅ Validation input
        if (email.isEmpty()) {
            message.setValue("Vui lòng nhập email");
            return;
        }

        // ✅ Set loading state
        loading.setValue(true);

        // ✅ Tạo request object
        ForgotPasswordRequest request = new ForgotPasswordRequest(email);

        // ✅ Gọi API qua Repository
        repository.forgotPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                loading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Gửi OTP thành công
                    otpSent.setValue(true);
                    message.setValue(response.body().getMessage());
                } else {
                    // ❌ Lỗi từ server
                    message.setValue("Gửi OTP thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                loading.setValue(false);
                // ❌ Lỗi network
                message.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    /**
     * ✅ Đặt lại mật khẩu với OTP
     * @param email Email người dùng
     * @param otp Mã OTP
     * @param newPassword Mật khẩu mới
     * @param confirmPassword Xác nhận mật khẩu mới
     */
    public void resetPassword(String email, String otp, String newPassword, String confirmPassword) {
        // ✅ Validation input
        if (email.isEmpty()) {
            message.setValue("Email không hợp lệ");
            return;
        }

        if (otp.isEmpty()) {
            message.setValue("Vui lòng nhập mã OTP");
            return;
        }

        if (newPassword.isEmpty()) {
            message.setValue("Vui lòng nhập mật khẩu mới");
            return;
        }

        if (confirmPassword.isEmpty()) {
            message.setValue("Vui lòng xác nhận mật khẩu");
            return;
        }

        // ✅ Kiểm tra password matching
        if (!newPassword.equals(confirmPassword)) {
            message.setValue("Mật khẩu xác nhận không khớp");
            return;
        }

        // ✅ Set loading state
        loading.setValue(true);

        // ✅ Tạo request object
        ResetPasswordRequest request = new ResetPasswordRequest(email, otp, newPassword);

        // ✅ Gọi API qua Repository
        repository.resetPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                loading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    // ✅ Reset password thành công
                    resetSuccess.setValue(true);
                    message.setValue(response.body().getMessage());
                } else {
                    // ❌ Lỗi từ server
                    message.setValue("Đặt lại mật khẩu thất bại: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                loading.setValue(false);
                // ❌ Lỗi network
                message.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Xử lý cả 2 chức năng (send OTP và reset password) trong 1 ViewModel
- ✅ **ĐIỂM MẠNH:** Có validation password matching
- ⚠️ **VẤN ĐỀ:** Không validate format OTP (6 digits)

---

## 🗄️ PHẦN 3: REPOSITORY (1 FILE)

### 3.1. AuthRepository.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/repository/AuthRepository.java`

**Chức năng:** Trung gian giữa ViewModel và API Service, quản lý các API calls

```java
package com.example.emotiondebugging.repository;

import com.example.emotiondebugging.api.ApiClient;
import com.example.emotiondebugging.api.ApiService;
import com.example.emotiondebugging.model.request.ForgotPasswordRequest;
import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.request.RegisterRequest;
import com.example.emotiondebugging.model.request.ResetPasswordRequest;
import com.example.emotiondebugging.model.response.LoginResponse;
import com.example.emotiondebugging.model.response.MessageResponse;
import com.example.emotiondebugging.model.response.RegisterResponse;

import retrofit2.Call;

/**
 * ✅ Repository Pattern: Tách biệt logic gọi API khỏi ViewModel
 * Giúp code dễ test và maintain hơn
 */
public class AuthRepository {

    // ✅ Lấy instance của ApiService từ ApiClient
    private final ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

    /**
     * ✅ Gọi API đăng nhập
     * @param request LoginRequest object chứa account và password
     * @return Call<LoginResponse> để ViewModel xử lý async
     */
    public Call<LoginResponse> login(LoginRequest request) {
        return apiService.login(request);
    }

    /**
     * ✅ Gọi API đăng ký
     * @param request RegisterRequest object chứa studentCode, email, password
     * @return Call<RegisterResponse>
     */
    public Call<RegisterResponse> register(RegisterRequest request) {
        return apiService.register(request);
    }

    /**
     * ✅ Gọi API gửi OTP (forgot password)
     * @param request ForgotPasswordRequest object chứa email
     * @return Call<MessageResponse>
     */
    public Call<MessageResponse> forgotPassword(ForgotPasswordRequest request) {
        return apiService.forgotPassword(request);
    }

    /**
     * ✅ Gọi API reset password
     * @param request ResetPasswordRequest object chứa email, otp, newPassword
     * @return Call<MessageResponse>
     */
    public Call<MessageResponse> resetPassword(ResetPasswordRequest request) {
        return apiService.resetPassword(request);
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Áp dụng Repository Pattern đúng chuẩn
- ✅ **ĐIỂM MẠNH:** Tách biệt logic API khỏi ViewModel
- ✅ **ĐIỂM MẠNH:** Dễ dàng mock để test

---

## 🌐 PHẦN 4: API SERVICE (2 FILES)

### 4.1. ApiService.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/api/ApiService.java`

**Chức năng:** Định nghĩa các API endpoints sử dụng Retrofit

```java
package com.example.emotiondebugging.api;

import com.example.emotiondebugging.model.request.ForgotPasswordRequest;
import com.example.emotiondebugging.model.request.LoginRequest;
import com.example.emotiondebugging.model.request.RegisterRequest;
import com.example.emotiondebugging.model.request.ResetPasswordRequest;
import com.example.emotiondebugging.model.response.LoginResponse;
import com.example.emotiondebugging.model.response.MessageResponse;
import com.example.emotiondebugging.model.response.RegisterResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * ✅ Retrofit API Interface
 * Định nghĩa các endpoints và HTTP methods
 */
public interface ApiService {

    /**
     * ✅ POST /api/auth/login
     * Đăng nhập với account (email hoặc student code) và password
     */
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    /**
     * ✅ POST /api/auth/register
     * Đăng ký tài khoản mới với studentCode, email, password
     */
    @POST("api/auth/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    /**
     * ✅ POST /api/auth/forgot-password
     * Gửi OTP về email để reset password
     */
    @POST("api/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ForgotPasswordRequest request);

    /**
     * ✅ POST /api/auth/reset-password
     * Đặt lại mật khẩu với OTP và password mới
     */
    @POST("api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest request);
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Sử dụng Retrofit annotations đúng chuẩn
- ✅ **ĐIỂM MẠNH:** Endpoints rõ ràng và RESTful
- ⚠️ **VẤN ĐỀ:** Không có interceptor để log request/response (khó debug)

---

### 4.2. ApiClient.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/api/ApiClient.java`

**Chức năng:** Cấu hình Retrofit client (base URL, converters, timeouts)

```java
package com.example.emotiondebugging.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * ✅ Singleton Retrofit Client
 * Tạo và quản lý instance duy nhất của Retrofit
 */
public class ApiClient {

    // ✅ Base URL của backend API
    private static final String BASE_URL = "http://10.0.2.2:3000/";
    
    // ✅ Singleton instance
    private static Retrofit retrofit = null;

    /**
     * ✅ Lấy hoặc tạo Retrofit instance
     * @return Retrofit instance đã được cấu hình
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    // ✅ Sử dụng Gson để convert JSON <-> Java objects
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Áp dụng Singleton Pattern
- ⚠️ **VẤN ĐỀ QUAN TRỌNG:** BASE_URL hardcoded là `10.0.2.2` (Android Emulator localhost)
- ⚠️ **HẬU QUẢ:** Không chạy được trên thiết bị thật
- ⚠️ **VẤN ĐỀ:** Không có timeout configuration
- ⚠️ **VẤN ĐỀ:** Không có logging interceptor để debug

**✅ GIẢI PHÁP ĐỀ XUẤT:**
```java
// Thêm OkHttpClient với logging và timeout
OkHttpClient client = new OkHttpClient.Builder()
    .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build();

retrofit = new Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build();
```

---

## 📦 PHẦN 5: MODELS - REQUEST (5 FILES)

### 5.1. LoginRequest.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/request/LoginRequest.java`

```java
package com.example.emotiondebugging.model.request;

/**
 * ✅ Request model cho API login
 */
public class LoginRequest {
    private String account;  // Email hoặc student code
    private String password; // Mật khẩu

    public LoginRequest(String account, String password) {
        this.account = account;
        this.password = password;
    }

    // Getters and Setters
    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

---

### 5.2. RegisterRequest.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/request/RegisterRequest.java`

```java
package com.example.emotiondebugging.model.request;

/**
 * ✅ Request model cho API register
 */
public class RegisterRequest {
    private String studentCode; // Mã sinh viên
    private String email;       // Email
    private String password;    // Mật khẩu

    public RegisterRequest(String studentCode, String email, String password) {
        this.studentCode = studentCode;
        this.email = email;
        this.password = password;
    }

    // Getters and Setters
    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

---

### 5.3. ForgotPasswordRequest.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/request/ForgotPasswordRequest.java`

```java
package com.example.emotiondebugging.model.request;

/**
 * ✅ Request model cho API forgot-password (gửi OTP)
 */
public class ForgotPasswordRequest {
    private String email; // Email nhận OTP

    public ForgotPasswordRequest(String email) {
        this.email = email;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

---

### 5.4. ResetPasswordRequest.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/request/ResetPasswordRequest.java`

```java
package com.example.emotiondebugging.model.request;

/**
 * ✅ Request model cho API reset-password
 */
public class ResetPasswordRequest {
    private String email;       // Email
    private String otp;         // Mã OTP
    private String newPassword; // Mật khẩu mới

    public ResetPasswordRequest(String email, String otp, String newPassword) {
        this.email = email;
        this.otp = otp;
        this.newPassword = newPassword;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
```

---

### 5.5. VerifyOTPRequest.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/request/VerifyOTPRequest.java`

```java
package com.example.emotiondebugging.model.request;

/**
 * ✅ Request model cho API verify-otp (nếu có endpoint riêng)
 * ⚠️ LƯU Ý: File này có thể không được sử dụng nếu verify OTP được tích hợp trong reset-password
 */
public class VerifyOTPRequest {
    private String email; // Email
    private String otp;   // Mã OTP

    public VerifyOTPRequest(String email, String otp) {
        this.email = email;
        this.otp = otp;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
```

---

## 📥 PHẦN 6: MODELS - RESPONSE (3 FILES)

### 6.1. LoginResponse.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/response/LoginResponse.java`

```java
package com.example.emotiondebugging.model.response;

/**
 * ✅ Response model cho API login
 */
public class LoginResponse {
    private String token;   // JWT token
    private User user;      // Thông tin user
    private String message; // Thông báo từ server

    // Getters and Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * ✅ Inner class User - Thông tin người dùng
     */
    public static class User {
        private String id;          // User ID
        private String studentCode; // Mã sinh viên
        private String email;       // Email
        private String role;        // Role (student, teacher, admin)

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStudentCode() {
            return studentCode;
        }

        public void setStudentCode(String studentCode) {
            this.studentCode = studentCode;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Có nested User class để chứa thông tin user
- ⚠️ **VẤN ĐỀ:** Không có @SerializedName annotations (có thể gây lỗi nếu backend dùng snake_case)

---

### 6.2. RegisterResponse.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/response/RegisterResponse.java`

```java
package com.example.emotiondebugging.model.response;

/**
 * ✅ Response model cho API register
 */
public class RegisterResponse {
    private String message; // Thông báo từ server
    private User user;      // Thông tin user vừa đăng ký (optional)

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * ✅ Inner class User
     */
    public static class User {
        private String id;
        private String studentCode;
        private String email;

        // Getters and Setters
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStudentCode() {
            return studentCode;
        }

        public void setStudentCode(String studentCode) {
            this.studentCode = studentCode;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}
```

---

### 6.3. MessageResponse.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/model/response/MessageResponse.java`

```java
package com.example.emotiondebugging.model.response;

/**
 * ✅ Generic response model cho các API chỉ trả về message
 * Sử dụng cho: forgot-password, reset-password, verify-otp
 */
public class MessageResponse {
    private String message; // Thông báo từ server

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Reusable cho nhiều endpoints
- ✅ **ĐIỂM MẠNH:** Đơn giản và dễ maintain

---

## 🎨 PHẦN 7: LAYOUTS XML (5 FILES)

### 7.1. activity_start_screen.xml
**Đường dẫn:** `android-app/app/src/main/res/layout/activity_start_screen.xml`

**Chức năng:** Layout cho màn hình khởi động (logo + progress bar)

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/gradient_background"
    android:padding="24dp">

    <!-- ✅ Logo app ở giữa màn hình -->
    <ImageView
        android:id="@+id/imgLogo"
        android:layout_width="200dp"
        android:layout_height="200dp"
        android:layout_centerInParent="true"
        android:src="@drawable/ic_logo"
        android:contentDescription="@string/app_logo" />

    <!-- ✅ Progress bar loading ở dưới logo -->
    <ProgressBar
        android:id="@+id/progressBar"
        style="?android:attr/progressBarStyleHorizontal"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_below="@id/imgLogo"
        android:layout_marginTop="32dp"
        android:max="100"
        android:progress="0"
        android:progressDrawable="@drawable/progress_bar_style" />

    <!-- ✅ Text "Loading..." -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_below="@id/progressBar"
        android:layout_centerHorizontal="true"
        android:layout_marginTop="16dp"
        android:text="@string/loading"
        android:textColor="@android:color/white"
        android:textSize="16sp" />

</RelativeLayout>
```

**📌 Components:**
- ImageView: Logo app
- ProgressBar: Thanh loading
- TextView: Text "Loading..."

---

### 7.2. activity_login.xml
**Đường dẫn:** `android-app/app/src/main/res/layout/activity_login.xml`

**Chức năng:** Layout cho màn hình đăng nhập

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/gradient_background"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp"
        android:gravity="center">

        <!-- ✅ Logo -->
        <ImageView
            android:layout_width="120dp"
            android:layout_height="120dp"
            android:layout_marginTop="48dp"
            android:src="@drawable/ic_logo"
            android:contentDescription="@string/app_logo" />

        <!-- ✅ Title "Đăng nhập" -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:text="@string/login_title"
            android:textColor="@android:color/white"
            android:textSize="28sp"
            android:textStyle="bold" />

        <!-- ✅ Input tài khoản -->
        <EditText
            android:id="@+id/etAccount"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp"
            android:background="@drawable/input_background"
            android:hint="@string/account_hint"
            android:inputType="textEmailAddress"
            android:padding="16dp"
            android:textColor="@android:color/white"
            android:textColorHint="@android:color/white" />

        <!-- ✅ Input mật khẩu với icon show/hide -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp">

            <EditText
                android:id="@+id/etPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/input_background"
                android:hint="@string/password_hint"
                android:inputType="textPassword"
                android:padding="16dp"
                android:textColor="@android:color/white"
                android:textColorHint="@android:color/white" />

            <ImageView
                android:id="@+id/imgTogglePassword"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:layout_marginEnd="16dp"
                android:src="@drawable/ic_eye"
                android:contentDescription="@string/toggle_password" />

        </RelativeLayout>

        <!-- ✅ Text "Quên mật khẩu?" -->
        <TextView
            android:id="@+id/tvForgotPassword"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="end"
            android:layout_marginTop="8dp"
            android:text="@string/forgot_password"
            android:textColor="@android:color/white"
            android:textSize="14sp" />

        <!-- ✅ Nút đăng nhập -->
        <Button
            android:id="@+id/btnLogin"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:background="@drawable/button_background"
            android:text="@string/login_button"
            android:textColor="@android:color/white"
            android:textSize="16sp"
            android:textStyle="bold" />

        <!-- ✅ Text "Chưa có tài khoản? Đăng ký" -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/no_account"
                android:textColor="@android:color/white"
                android:textSize="14sp" />

            <TextView
                android:id="@+id/tvRegister"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:text="@string/register_link"
                android:textColor="@android:color/white"
                android:textSize="14sp"
                android:textStyle="bold" />

        </LinearLayout>

    </LinearLayout>

</ScrollView>
```

**📌 Components:**
- EditText: Account input
- EditText + ImageView: Password input với toggle visibility
- TextView: Forgot password link
- Button: Login button
- TextView: Register link

---

### 7.3. activity_register.xml
**Đường dẫn:** `android-app/app/src/main/res/layout/activity_register.xml`

**Chức năng:** Layout cho màn hình đăng ký

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/gradient_background"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp"
        android:gravity="center">

        <!-- ✅ Logo -->
        <ImageView
            android:layout_width="100dp"
            android:layout_height="100dp"
            android:layout_marginTop="32dp"
            android:src="@drawable/ic_logo"
            android:contentDescription="@string/app_logo" />

        <!-- ✅ Title "Đăng ký" -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="@string/register_title"
            android:textColor="@android:color/white"
            android:textSize="28sp"
            android:textStyle="bold" />

        <!-- ✅ Input mã sinh viên -->
        <EditText
            android:id="@+id/etStudentCode"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:background="@drawable/input_background"
            android:hint="@string/student_code_hint"
            android:inputType="text"
            android:padding="16dp"
            android:textColor="@android:color/white"
            android:textColorHint="@android:color/white" />

        <!-- ✅ Input email -->
        <EditText
            android:id="@+id/etEmail"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:background="@drawable/input_background"
            android:hint="@string/email_hint"
            android:inputType="textEmailAddress"
            android:padding="16dp"
            android:textColor="@android:color/white"
            android:textColorHint="@android:color/white" />

        <!-- ✅ Input mật khẩu -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp">

            <EditText
                android:id="@+id/etPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/input_background"
                android:hint="@string/password_hint"
                android:inputType="textPassword"
                android:padding="16dp"
                android:textColor="@android:color/white"
                android:textColorHint="@android:color/white" />

            <ImageView
                android:id="@+id/imgTogglePassword"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:layout_marginEnd="16dp"
                android:src="@drawable/ic_eye"
                android:contentDescription="@string/toggle_password" />

        </RelativeLayout>

        <!-- ✅ Input xác nhận mật khẩu -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp">

            <EditText
                android:id="@+id/etConfirmPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/input_background"
                android:hint="@string/confirm_password_hint"
                android:inputType="textPassword"
                android:padding="16dp"
                android:textColor="@android:color/white"
                android:textColorHint="@android:color/white" />

            <ImageView
                android:id="@+id/imgToggleConfirmPassword"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:layout_marginEnd="16dp"
                android:src="@drawable/ic_eye"
                android:contentDescription="@string/toggle_password" />

        </RelativeLayout>

        <!-- ✅ Nút đăng ký -->
        <Button
            android:id="@+id/btnRegister"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:background="@drawable/button_background"
            android:text="@string/register_button"
            android:textColor="@android:color/white"
            android:textSize="16sp"
            android:textStyle="bold" />

        <!-- ✅ Text "Đã có tài khoản? Đăng nhập" -->
        <LinearLayout
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:orientation="horizontal">

            <TextView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/have_account"
                android:textColor="@android:color/white"
                android:textSize="14sp" />

            <TextView
                android:id="@+id/tvLogin"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="4dp"
                android:text="@string/login_link"
                android:textColor="@android:color/white"
                android:textSize="14sp"
                android:textStyle="bold" />

        </LinearLayout>

    </LinearLayout>

</ScrollView>
```

**📌 Components:**
- EditText: Student code, email, password, confirm password inputs
- ImageView: Toggle password visibility icons
- Button: Register button
- TextView: Login link

---

### 7.4. activity_forgot_password.xml
**Đường dẫn:** `android-app/app/src/main/res/layout/activity_forgot_password.xml`

**Chức năng:** Layout cho màn hình quên mật khẩu

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/gradient_background"
    android:orientation="vertical"
    android:padding="24dp">

    <!-- ✅ Back button -->
    <ImageView
        android:id="@+id/imgBack"
        android:layout_width="32dp"
        android:layout_height="32dp"
        android:src="@drawable/ic_back"
        android:contentDescription="@string/back_button" />

    <!-- ✅ Title "Quên mật khẩu" -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:text="@string/forgot_password_title"
        android:textColor="@android:color/white"
        android:textSize="28sp"
        android:textStyle="bold" />

    <!-- ✅ Description -->
    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:text="@string/forgot_password_description"
        android:textColor="@android:color/white"
        android:textSize="14sp" />

    <!-- ✅ Input email -->
    <EditText
        android:id="@+id/etEmail"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="32dp"
        android:background="@drawable/input_background"
        android:hint="@string/email_hint"
        android:inputType="textEmailAddress"
        android:padding="16dp"
        android:textColor="@android:color/white"
        android:textColorHint="@android:color/white" />

    <!-- ✅ Nút gửi OTP -->
    <Button
        android:id="@+id/btnSendOTP"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:background="@drawable/button_background"
        android:text="@string/send_otp_button"
        android:textColor="@android:color/white"
        android:textSize="16sp"
        android:textStyle="bold" />

</LinearLayout>
```

**📌 Components:**
- ImageView: Back button
- TextView: Title và description
- EditText: Email input
- Button: Send OTP button

---

### 7.5. activity_reset_password.xml
**Đường dẫn:** `android-app/app/src/main/res/layout/activity_reset_password.xml`

**Chức năng:** Layout cho màn hình đặt lại mật khẩu

```xml
<?xml version="1.0" encoding="utf-8"?>
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/gradient_background"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:padding="24dp">

        <!-- ✅ Back button -->
        <ImageView
            android:id="@+id/imgBack"
            android:layout_width="32dp"
            android:layout_height="32dp"
            android:src="@drawable/ic_back"
            android:contentDescription="@string/back_button" />

        <!-- ✅ Title "Đặt lại mật khẩu" -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp"
            android:text="@string/reset_password_title"
            android:textColor="@android:color/white"
            android:textSize="28sp"
            android:textStyle="bold" />

        <!-- ✅ Description -->
        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="8dp"
            android:text="@string/reset_password_description"
            android:textColor="@android:color/white"
            android:textSize="14sp" />

        <!-- ✅ Input OTP -->
        <EditText
            android:id="@+id/etOTP"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="32dp"
            android:background="@drawable/input_background"
            android:hint="@string/otp_hint"
            android:inputType="number"
            android:maxLength="6"
            android:padding="16dp"
            android:textColor="@android:color/white"
            android:textColorHint="@android:color/white" />

        <!-- ✅ Input mật khẩu mới -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp">

            <EditText
                android:id="@+id/etNewPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/input_background"
                android:hint="@string/new_password_hint"
                android:inputType="textPassword"
                android:padding="16dp"
                android:textColor="@android:color/white"
                android:textColorHint="@android:color/white" />

            <ImageView
                android:id="@+id/imgTogglePassword"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:layout_marginEnd="16dp"
                android:src="@drawable/ic_eye"
                android:contentDescription="@string/toggle_password" />

        </RelativeLayout>

        <!-- ✅ Input xác nhận mật khẩu mới -->
        <RelativeLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="16dp">

            <EditText
                android:id="@+id/etConfirmPassword"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="@drawable/input_background"
                android:hint="@string/confirm_password_hint"
                android:inputType="textPassword"
                android:padding="16dp"
                android:textColor="@android:color/white"
                android:textColorHint="@android:color/white" />

            <ImageView
                android:id="@+id/imgToggleConfirmPassword"
                android:layout_width="24dp"
                android:layout_height="24dp"
                android:layout_alignParentEnd="true"
                android:layout_centerVertical="true"
                android:layout_marginEnd="16dp"
                android:src="@drawable/ic_eye"
                android:contentDescription="@string/toggle_password" />

        </RelativeLayout>

        <!-- ✅ Nút đặt lại mật khẩu -->
        <Button
            android:id="@+id/btnResetPassword"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="24dp"
            android:background="@drawable/button_background"
            android:text="@string/reset_password_button"
            android:textColor="@android:color/white"
            android:textSize="16sp"
            android:textStyle="bold" />

    </LinearLayout>

</ScrollView>
```

**📌 Components:**
- ImageView: Back button
- EditText: OTP input (6 digits)
- EditText: New password và confirm password inputs
- ImageView: Toggle password visibility icons
- Button: Reset password button

---

## 🎨 PHẦN 8: DRAWABLES XML (8 FILES)

### 8.1. gradient_background.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/gradient_background.xml`

**Chức năng:** Background gradient cho tất cả màn hình authentication

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <gradient
        android:angle="135"
        android:startColor="#667eea"
        android:endColor="#764ba2"
        android:type="linear" />
</shape>
```

---

### 8.2. input_background.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/input_background.xml`

**Chức năng:** Background cho các EditText inputs

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#40FFFFFF" />
    <corners android:radius="12dp" />
    <stroke
        android:width="1dp"
        android:color="#80FFFFFF" />
</shape>
```

---

### 8.3. button_background.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/button_background.xml`

**Chức năng:** Background cho các buttons

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="#FFFFFF" />
    <corners android:radius="12dp" />
</shape>
```

---

### 8.4. button_background_pressed.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/button_background_pressed.xml`

**Chức năng:** Selector cho button states (normal, pressed, disabled)

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape>
            <solid android:color="#E0E0E0" />
            <corners android:radius="12dp" />
        </shape>
    </item>
    <item android:state_enabled="false">
        <shape>
            <solid android:color="#80FFFFFF" />
            <corners android:radius="12dp" />
        </shape>
    </item>
    <item>
        <shape>
            <solid android:color="#FFFFFF" />
            <corners android:radius="12dp" />
        </shape>
    </item>
</selector>
```

---

### 8.5. progress_bar_style.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/progress_bar_style.xml`

**Chức năng:** Custom style cho progress bar trong StartScreen

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Background -->
    <item android:id="@android:id/background">
        <shape>
            <solid android:color="#40FFFFFF" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <!-- Progress -->
    <item android:id="@android:id/progress">
        <clip>
            <shape>
                <solid android:color="#FFFFFF" />
                <corners android:radius="8dp" />
            </shape>
        </clip>
    </item>
</layer-list>
```

---

### 8.6. ic_logo.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/ic_logo.xml`

**Chức năng:** Vector drawable cho logo app

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="200dp"
    android:height="200dp"
    android:viewportWidth="200"
    android:viewportHeight="200">
    
    <!-- ⚠️ LƯU Ý: Đây là placeholder, cần thay bằng logo thật -->
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M100,50 L150,100 L100,150 L50,100 Z" />
    
</vector>
```

---

### 8.7. ic_eye.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/ic_eye.xml`

**Chức năng:** Icon con mắt để toggle password visibility

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M12,4.5C7,4.5 2.73,7.61 1,12c1.73,4.39 6,7.5 11,7.5s9.27,-3.11 11,-7.5c-1.73,-4.39 -6,-7.5 -11,-7.5zM12,17c-2.76,0 -5,-2.24 -5,-5s2.24,-5 5,-5 5,2.24 5,5 -2.24,5 -5,5zM12,9c-1.66,0 -3,1.34 -3,3s1.34,3 3,3 3,-1.34 3,-3 -1.34,-3 -3,-3z" />
    
</vector>
```

---

### 8.8. ic_back.xml
**Đường dẫn:** `android-app/app/src/main/res/drawable/ic_back.xml`

**Chức năng:** Icon back arrow

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    
    <path
        android:fillColor="#FFFFFF"
        android:pathData="M20,11H7.83l5.59,-5.59L12,4l-8,8 8,8 1.41,-1.41L7.83,13H20v-2z" />
    
</vector>
```

---

## 🛠️ PHẦN 9: UTILS (1 FILE)

### 9.1. SharedPrefsHelper.java
**Đường dẫn:** `android-app/app/src/main/java/com/example/emotiondebugging/utils/SharedPrefsHelper.java`

**Chức năng:** Lưu trữ và quản lý token, user info trong SharedPreferences

```java
package com.example.emotiondebugging.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * ✅ Helper class để lưu trữ dữ liệu persistent (token, user info)
 * Sử dụng SharedPreferences - key-value storage của Android
 */
public class SharedPrefsHelper {

    private static final String PREF_NAME = "EmotionDebuggingPrefs";
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
        editor.apply();
    }

    /**
     * ✅ Lấy token đã lưu
     * @return Token string hoặc null nếu chưa login
     */
    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    /**
     * ✅ Lưu thông tin user
     */
    public void saveUserInfo(String userId, String email, String studentCode, String role) {
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_EMAIL, email);
        editor.putString(KEY_STUDENT_CODE, studentCode);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    /**
     * ✅ Lấy user ID
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * ✅ Lấy email
     */
    public String getEmail() {
        return prefs.getString(KEY_EMAIL, null);
    }

    /**
     * ✅ Lấy student code
     */
    public String getStudentCode() {
        return prefs.getString(KEY_STUDENT_CODE, null);
    }

    /**
     * ✅ Lấy role
     */
    public String getRole() {
        return prefs.getString(KEY_ROLE, null);
    }

    /**
     * ✅ Kiểm tra user đã login chưa
     * @return true nếu đã login, false nếu chưa
     */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /**
     * ✅ Xóa tất cả dữ liệu (logout)
     */
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    /**
     * ✅ Logout - Xóa token và set isLoggedIn = false
     */
    public void logout() {
        editor.remove(KEY_TOKEN);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }
}
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Có đầy đủ methods để lưu/lấy token và user info
- ✅ **ĐIỂM MẠNH:** Có method isLoggedIn() để check authentication state
- ⚠️ **VẤN ĐỀ:** Class này CHƯA ĐƯỢC SỬ DỤNG trong LoginActivity!
- ⚠️ **HẬU QUẢ:** Token không được lưu, user phải login lại mỗi lần mở app

---

## 📱 PHẦN 10: ANDROIDMANIFEST.XML (1 FILE)

### 10.1. AndroidManifest.xml
**Đường dẫn:** `android-app/app/src/main/AndroidManifest.xml`

**Chức năng:** Cấu hình app, khai báo activities, permissions

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.emotiondebugging">

    <!-- ✅ Permission để truy cập internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.EmotionDebugging"
        android:usesCleartextTraffic="true">

        <!-- ✅ StartScreenActivity - Launcher activity (màn hình đầu tiên) -->
        <activity
            android:name=".ui.splash.StartScreenActivity"
            android:exported="true"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- ✅ LoginActivity -->
        <activity
            android:name=".ui.auth.LoginActivity"
            android:exported="false"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar" />

        <!-- ✅ RegisterActivity -->
        <activity
            android:name=".ui.auth.RegisterActivity"
            android:exported="false"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar" />

        <!-- ✅ ForgotPasswordActivity -->
        <activity
            android:name=".ui.auth.ForgotPasswordActivity"
            android:exported="false"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar" />

        <!-- ✅ ResetPasswordActivity -->
        <activity
            android:name=".ui.auth.ResetPasswordActivity"
            android:exported="false"
            android:theme="@style/Theme.EmotionDebugging.NoActionBar" />

    </application>

</manifest>
```

**📌 Lưu ý quan trọng:**
- ✅ **ĐIỂM MẠNH:** Có permission INTERNET và ACCESS_NETWORK_STATE
- ✅ **ĐIỂM MẠNH:** usesCleartextTraffic="true" cho phép HTTP (cần cho development)
- ⚠️ **VẤN ĐỀ BẢO MẬT:** usesCleartextTraffic nên tắt trong production
- ✅ **ĐIỂM MẠNH:** StartScreenActivity là LAUNCHER activity

---

## 📊 TỔNG KẾT VÀ ĐÁNH GIÁ

### ✅ ĐIỂM MẠNH

1. **Kiến trúc MVVM chuẩn:**
   - Tách biệt rõ ràng: Activity (View) - ViewModel - Repository - API
   - Sử dụng LiveData để observe data changes
   - Repository Pattern để tách logic API

2. **UI/UX đẹp và nhất quán:**
   - Gradient background đồng nhất
   - Custom drawables cho inputs và buttons
   - Toggle password visibility
   - Loading states với disable buttons

3. **Validation đầy đủ:**
   - Check empty fields
   - Password matching validation
   - Error messages rõ ràng

4. **Navigation flow hợp lý:**
   - Start Screen → Login → Register/Forgot Password
   - Forgot Password → Reset Password → Login
   - Register → Login

### ⚠️ VẤN ĐỀ NGHIÊM TRỌNG CẦN SỬA NGAY

#### 1. **KHÔNG LƯU TOKEN SAU KHI LOGIN** ❌
**File:** `LoginActivity.java`
**Vấn đề:** Method `handleLoginSuccess()` không lưu token vào SharedPrefs
**Hậu quả:** User phải login lại mỗi lần mở app

**Giải pháp:**
```java
private void handleLoginSuccess(LoginResponse response) {
    // ✅ Lưu token và user info
    SharedPrefsHelper prefsHelper = new SharedPrefsHelper(this);
    prefsHelper.saveToken(response.getToken());
    prefsHelper.saveUserInfo(
        response.getUser().getId(),
        response.getUser().getEmail(),
        response.getUser().getStudentCode(),
        response.getUser().getRole()
    );
    
    // ✅ Chuyển sang MainActivity
    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
}
```

#### 2. **KHÔNG CHECK AUTHENTICATION STATE KHI MỞ APP** ❌
**File:** `StartScreenActivity.java`
**Vấn đề:** Luôn chuyển sang LoginActivity, không check token
**Hậu quả:** User đã login vẫn phải login lại

**Giải pháp:**
```java
animation.addListener(new android.animation.AnimatorListenerAdapter() {
    @Override
    public void onAnimationEnd(android.animation.Animator animation) {
        // ✅ Check authentication state
        SharedPrefsHelper prefsHelper = new SharedPrefsHelper(StartScreenActivity.this);
        
        Intent intent;
        if (prefsHelper.isLoggedIn() && prefsHelper.getToken() != null) {
            // ✅ Đã login -> Chuyển sang MainActivity
            intent = new Intent(StartScreenActivity.this, MainActivity.class);
        } else {
            // ❌ Chưa login -> Chuyển sang LoginActivity
            intent = new Intent(StartScreenActivity.this, LoginActivity.class);
        }
        
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
});
```

#### 3. **BASE_URL HARDCODED CHO EMULATOR** ⚠️
**File:** `ApiClient.java`
**Vấn đề:** `BASE_URL = "http://10.0.2.2:3000/"` chỉ chạy trên emulator
**Hậu quả:** Không chạy được trên thiết bị thật

**Giải pháp:**
```java
// ✅ Sử dụng BuildConfig để switch giữa dev và production
private static final String BASE_URL = BuildConfig.DEBUG 
    ? "http://10.0.2.2:3000/"  // Emulator
    : "https://api.yourdomain.com/";  // Production
```

#### 4. **KHÔNG CÓ LOGGING INTERCEPTOR** ⚠️
**File:** `ApiClient.java`
**Vấn đề:** Không log request/response, khó debug
**Giải pháp:** Thêm HttpLoggingInterceptor (đã nêu ở phần ApiClient)

#### 5. **KHÔNG CÓ ERROR PARSING** ⚠️
**File:** Tất cả ViewModels
**Vấn đề:** Chỉ hiển thị `response.message()`, không parse error body từ server
**Giải pháp:**
```java
if (!response.isSuccessful()) {
    try {
        String errorBody = response.errorBody().string();
        JSONObject errorJson = new JSONObject(errorBody);
        String errorMessage = errorJson.getString("message");
        message.setValue(errorMessage);
    } catch (Exception e) {
        message.setValue("Lỗi: " + response.message());
    }
}
```

### 📈 ĐỀ XUẤT CẢI TIẾN

1. **Thêm input validation nâng cao:**
   - Email format validation
   - Password strength validation (min 8 chars, có số, có ký tự đặc biệt)
   - OTP format validation (6 digits)

2. **Thêm token refresh mechanism:**
   - Interceptor để tự động refresh token khi hết hạn
   - Logout tự động khi refresh token fail

3. **Thêm biometric authentication:**
   - Fingerprint/Face ID để login nhanh

4. **Cải thiện UX:**
   - Remember me checkbox
   - Auto-fill OTP từ SMS
   - Countdown timer cho resend OTP

5. **Security improvements:**
   - Certificate pinning
   - Encrypt token trong SharedPrefs
   - Tắt usesCleartextTraffic trong production

---

## 🎯 CHECKLIST SỬA LỖI ƯU TIÊN

- [ ] **CRITICAL:** Implement lưu token trong LoginActivity
- [ ] **CRITICAL:** Implement check authentication trong StartScreenActivity
- [ ] **HIGH:** Thêm logging interceptor vào ApiClient
- [ ] **HIGH:** Implement error parsing trong ViewModels
- [ ] **MEDIUM:** Thêm email format validation
- [ ] **MEDIUM:** Thêm password strength validation
- [ ] **MEDIUM:** Config BASE_URL cho production
- [ ] **LOW:** Thêm @SerializedName annotations cho models
- [ ] **LOW:** Thêm timeout configuration cho Retrofit

---

**📅 Ngày tạo:** 2024
**👤 Tác giả:** Development Team
**📝 Phiên bản:** 1.0

