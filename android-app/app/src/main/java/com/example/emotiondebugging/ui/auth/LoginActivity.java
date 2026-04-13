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

    private EditText etAccount;
    private EditText etPassword;
    private ImageView imgTogglePassword;
    private Button btnLogin;
    private TextView tvForgotPassword;
    private TextView tvRegister;

    private boolean isPasswordVisible = false;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initViewModel();
        initActions();
    }

    private void initViews() {
        etAccount = findViewById(R.id.etAccount);
        etPassword = findViewById(R.id.etPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
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
            if (isLoading == null) return;

            btnLogin.setEnabled(!isLoading);
            btnLogin.setText(isLoading ? "Đang đăng nhập..." : "Đăng nhập");
        });

        viewModel.getLoginResponse().observe(this, response -> {
            if (response != null && response.getUser() != null) {
                handleLoginSuccess(response);
            }
        });
    }

    private void initActions() {
        imgTogglePassword.setOnClickListener(v -> togglePassword());

        btnLogin.setOnClickListener(v -> {
            String account = etAccount.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            viewModel.login(account, password);
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void handleLoginSuccess(LoginResponse response) {
        String role = response.getUser().getRole() != null ? response.getUser().getRole() : "";
        Toast.makeText(this, "Đăng nhập thành công - " + role, Toast.LENGTH_SHORT).show();
        // 1. Lưu Token và thông tin User
        com.example.emotiondebugging.utils.SharedPrefsHelper prefsHelper =
                new com.example.emotiondebugging.utils.SharedPrefsHelper(this);

        prefsHelper.saveToken(response.getToken());

        if (response.getUser() != null) {
            String userIdString = String.valueOf(response.getUser().getUserId());
            prefsHelper.saveUserInfo(
                    userIdString,
                    response.getUser().getEmail(),
                    response.getUser().getStudentCode(),
                    role,
                    response.getUser().getName()
            );
        }

        Intent intent;
        switch (role.toUpperCase()) {
            case "ADMIN":
                intent = new Intent(LoginActivity.this, com.example.emotiondebugging.ui.admin.AdminDashboardActivity.class);
                break;
            case "STAFF":
                intent = new Intent(LoginActivity.this, com.example.emotiondebugging.ui.staff.StaffDashboardActivity.class);
                break;
            case "STUDENT":
            default:
                intent = new Intent(LoginActivity.this, com.example.emotiondebugging.ui.main.MainActivity.class);
                break;
        }

        // Xóa lịch sử màn hình
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void togglePassword() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }

        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }
}