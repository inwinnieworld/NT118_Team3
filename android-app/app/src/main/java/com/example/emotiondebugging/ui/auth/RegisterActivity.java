package com.example.emotiondebugging.ui.auth;

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

    private EditText etFullName;
    private EditText etStudentCode;
    private EditText etEmail;
    private EditText etPassword;
    private ImageView imgTogglePassword;
    private Button btnRegister;
    private TextView tvLogin;

    private boolean isPasswordVisible = false;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initViewModel();
        initActions();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etStudentCode = findViewById(R.id.etStudentCode);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        imgTogglePassword = findViewById(R.id.imgTogglePassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading == null) return;
            btnRegister.setEnabled(!isLoading);
            btnRegister.setText(isLoading ? "Đang đăng ký..." : "Đăng ký");
        });

        viewModel.getSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                finish();
            }
        });
    }

    private void initActions() {
        imgTogglePassword.setOnClickListener(v -> togglePassword());

        btnRegister.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String studentCode = etStudentCode.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (fullName.isEmpty() || studentCode.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.register(fullName, email, password, studentCode);
        });

        tvLogin.setOnClickListener(v -> finish());
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