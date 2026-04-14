package com.example.emotiondebugging.ui.profile;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.ChangePasswordRequest;
import com.example.emotiondebugging.model.response.BaseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private boolean showOld = false, showNew = false, showConfirm = false;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        // Lấy token từ SharedPreferences
        com.example.emotiondebugging.utils.SharedPrefsHelper prefsHelper = 
            new com.example.emotiondebugging.utils.SharedPrefsHelper(this);
        String token = prefsHelper.getToken();
        if (token != null) {
            authToken = "Bearer " + token;
        }

        etOldPassword = findViewById(R.id.et_old_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_confirm).setOnClickListener(v -> submit());

        // Toggle hiện/ẩn mật khẩu
        ((ImageButton) findViewById(R.id.btn_toggle_old)).setOnClickListener(v -> {
            showOld = !showOld;
            toggleVisibility(etOldPassword, (ImageButton) v, showOld);
        });
        ((ImageButton) findViewById(R.id.btn_toggle_new)).setOnClickListener(v -> {
            showNew = !showNew;
            toggleVisibility(etNewPassword, (ImageButton) v, showNew);
        });
        ((ImageButton) findViewById(R.id.btn_toggle_confirm)).setOnClickListener(v -> {
            showConfirm = !showConfirm;
            toggleVisibility(etConfirmPassword, (ImageButton) v, showConfirm);
        });
    }

    private void toggleVisibility(EditText et, ImageButton btn, boolean show) {
        int pos = et.getSelectionEnd();
        if (show) {
            et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            btn.setImageResource(R.drawable.ic_eye_on);
        } else {
            et.setTransformationMethod(PasswordTransformationMethod.getInstance());
            btn.setImageResource(R.drawable.ic_eye_off);
        }
        et.setSelection(pos);
    }

    private void submit() {
        String oldPwd = etOldPassword.getText().toString().trim();
        String newPwd = etNewPassword.getText().toString().trim();
        String confirmPwd = etConfirmPassword.getText().toString().trim();

        if (oldPwd.isEmpty() || newPwd.isEmpty() || confirmPwd.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPwd.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPwd.equals(confirmPwd)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }

        RetrofitClient.getProfileApi()
                .changePassword(authToken, new ChangePasswordRequest(oldPwd, newPwd))
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(ChangePasswordActivity.this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String msg = "Đổi mật khẩu thất bại";
                            try {
                                // Parse error message từ server
                                org.json.JSONObject err = new org.json.JSONObject(response.errorBody().string());
                                msg = err.optString("message", msg);
                            } catch (Exception ignored) {}
                            Toast.makeText(ChangePasswordActivity.this, msg, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(ChangePasswordActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
