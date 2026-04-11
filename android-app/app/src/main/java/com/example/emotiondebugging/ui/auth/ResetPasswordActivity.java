package com.example.emotiondebugging.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.AuthApiService;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.ResetPasswordRequest;
import com.example.emotiondebugging.model.response.ApiResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText edtNewPassword;
    private EditText edtConfirmPassword;
    private Button btnResetPassword;
    private TextView txtBackToLogin;

    private String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        txtBackToLogin = findViewById(R.id.txtBackToLogin);

        readTokenFromDeepLink();

        txtBackToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });

        btnResetPassword.setOnClickListener(v -> handleResetPassword());
    }

    private void readTokenFromDeepLink() {
        Uri data = getIntent().getData();
        if (data != null) {
            token = data.getQueryParameter("token");
        }

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Liên kết không hợp lệ hoặc thiếu token", Toast.LENGTH_LONG).show();
        }
    }

    private void handleResetPassword() {
        String newPassword = edtNewPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(token)) {
            Toast.makeText(this, "Token không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu nhập lại không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        btnResetPassword.setEnabled(false);

        ResetPasswordRequest request = new ResetPasswordRequest(token, newPassword);
        AuthApiService apiService = RetrofitClient.getAuthApiService();

        apiService.resetPassword(request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                btnResetPassword.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Object> body = response.body();
                    Toast.makeText(ResetPasswordActivity.this, body.getMessage(), Toast.LENGTH_LONG).show();

                    if (body.isSuccess()) {
                        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thất bại", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                btnResetPassword.setEnabled(true);
                Toast.makeText(
                        ResetPasswordActivity.this,
                        t.getMessage() != null ? t.getMessage() : "Lỗi kết nối server",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}