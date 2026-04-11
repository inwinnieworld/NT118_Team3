package com.example.emotiondebugging.ui.admin;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.emotiondebugging.R;
import com.example.emotiondebugging.data.api.RetrofitClient;
import com.example.emotiondebugging.model.request.CreateStaffRequest;
import com.example.emotiondebugging.model.response.BaseResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddStaffActivity extends AppCompatActivity {

    private static final String TEST_TOKEN = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoyLCJlbWFpbCI6ImFkbWluQGdtYWlsLmNvbSIsImlhdCI6MTc3NTkxNDAwMCwiZXhwIjoxNzc2NTE4ODAwfQ.A5dD-yaFwRejbTVnn3Nuiw_FO7i5o3c5j8N7ee6V6QA";

    private EditText etName, etEmail, etPhone, etPassword, etPosition, etDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etPassword = findViewById(R.id.et_password);
        etPosition = findViewById(R.id.et_position);
        etDepartment = findViewById(R.id.et_department);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save).setOnClickListener(v -> save());
    }

    private void save() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String position = etPosition.getText().toString().trim();
        String department = etDepartment.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin bắt buộc", Toast.LENGTH_SHORT).show();
            return;
        }

        RetrofitClient.getAdminApi().createStaff(TEST_TOKEN,
                new CreateStaffRequest(name, email, phone, password, position, department))
                .enqueue(new Callback<BaseResponse>() {
                    @Override
                    public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddStaffActivity.this, "Tạo tài khoản thành công", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            try {
                                org.json.JSONObject err = new org.json.JSONObject(response.errorBody().string());
                                Toast.makeText(AddStaffActivity.this, err.optString("message", "Thất bại"), Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Toast.makeText(AddStaffActivity.this, "Tạo tài khoản thất bại", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<BaseResponse> call, Throwable t) {
                        Toast.makeText(AddStaffActivity.this, "Không kết nối được server", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
