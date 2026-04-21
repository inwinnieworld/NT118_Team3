package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.model.response.LoginResponse;
import com.example.emotiondebugging.ui.admin.AdminDashboardActivity;
import com.example.emotiondebugging.ui.staff.StaffDashboardActivity;
import com.example.emotiondebugging.ui.main.MainActivity;
import com.example.emotiondebugging.utils.SharedPrefsHelper;

public class LoginActivity extends AppCompatActivity {

    private EditText etAccount;
    private EditText etPassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;

    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        initViewModel();
        initActions();
    }

    private void initViews() {
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        viewModel.getMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                btnLogin.setEnabled(!isLoading);
                btnLogin.setText(isLoading ? "Đang đăng nhập..." : "Đăng nhập");
            }
        });

        viewModel.getLoginResponse().observe(this, response -> {
            if (response != null && response.getUser() != null) {
                handleLoginSuccess(response);
            }
        });

        viewModel.getLoginFormState().observe(this, state -> {
            if (state != null) {
                etAccount.setError(state.getAccountError());
                etPassword.setError(state.getPasswordError());
            }
        });
    }

    private void initActions() {
        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            etAccount.setError(null);
            etPassword.setError(null);

            viewModel.login(account, password);
        });

        tvForgotPassword.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class));
        });

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void handleLoginSuccess(LoginResponse response) {
        String role = response.getUser().getRole() != null ? response.getUser().getRole() : "";
        
        // ✅ LƯU TOKEN VÀ THÔNG TIN USER
        SharedPrefsHelper prefs = new SharedPrefsHelper(this);
        prefs.saveToken(response.getToken());
        prefs.saveUserInfo(
            String.valueOf(response.getUser().getUserId()),
            response.getUser().getEmail(),
            response.getUser().getStudentCode(),
            role,
            response.getUser().getName()
        );
        
        Toast.makeText(this, "Đăng nhập thành công - " + role, Toast.LENGTH_SHORT).show();

        Intent intent;
        switch (role.toUpperCase()) {
            case "ADMIN":
                intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                break;
            case "STAFF":
                // Check staff position - only "Nhân Viên Tạo Quest" can access Staff Dashboard
                String staffPosition = response.getUser().getStaffPosition();
                if ("Nhân Viên Tạo Quest".equals(staffPosition)) {
                    intent = new Intent(LoginActivity.this, StaffDashboardActivity.class);
                } else {
                    // Staff without quest creation permission - show error and stay on login
                    Toast.makeText(this, "Bạn không có quyền truy cập Staff Dashboard. Chỉ Nhân Viên Tạo Quest mới được phép.", Toast.LENGTH_LONG).show();
                    return; // Don't navigate, stay on login screen
                }
                break;
            case "STUDENT":
            default:
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
        }

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
