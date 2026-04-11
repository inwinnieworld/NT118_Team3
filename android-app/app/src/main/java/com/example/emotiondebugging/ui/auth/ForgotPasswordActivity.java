package com.example.emotiondebugging.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.emotiondebugging.R;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private Button btnSendRequest;
    private TextView tvBackToLogin;

    private ForgotPasswordViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        initViews();
        initViewModel();
        initActions();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        btnSendRequest = findViewById(R.id.btnSendRequest);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(ForgotPasswordViewModel.class);

        viewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading == null) return;
            btnSendRequest.setEnabled(!isLoading);
            btnSendRequest.setText(isLoading ? "Đang gửi..." : "Gửi yêu cầu");
        });

        viewModel.getSuccess().observe(this, success -> {
            if (Boolean.TRUE.equals(success)) {
                // Sau này có thể chuyển sang màn nhập OTP/reset password
            }
        });
    }

    private void initActions() {
        btnSendRequest.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.forgotPassword(email);
        });

        tvBackToLogin.setOnClickListener(v -> finish());
    }
}